package de.bigdata.semiClustering

import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * Implementierung des Semi-Clustering.
  *
  * @author Jakob Smedegaard Andersen
  */
object SemiClustering {

  /**
    * Implementiert das Semi-Clustering.
    *
    * Der Algorithmus basiert auf dem nachrichtenorientierten Modell von Pregel.
    *
    * @param graph der Graph auf dem operiert wird.
    * @param scoreFactor ein Faktor der bestimmt, wie stark aus dem Cluster herausragende Kanten in die Bewertung des Clusters einbezogen werden sollen.
    * @param maxIterations die Obergrenze an Iterationen die durchgefuehrt werden sollen.
    * @param maxClusterSize die maximale Groesse des Clusters.
    * @param maxCluster die maximale Anzahl an Clustern.
    *
    * @return ein Array bestehend aus den hoechst gewichteten Clustern.
    */
  def apply[VD: ClassTag](graph: Graph[VD, Double], scoreFactor: Double)(
    maxIterations: Int = Int.MaxValue,
    maxClusterSize: Int = Int.MaxValue,
    maxCluster: Int = Int.MaxValue): Array[(Double, List[VertexId])] = {

    val conGraph = {
      Graph(graph.vertices, graph.edges.filter(edge => edge.srcId != edge.dstId)).convertToCanonicalEdges(_ + _)
    }

    val edgesBoth: RDD[(VertexId, Array[SemiOutEdge])] = conGraph
      .collectEdges(EdgeDirection.Either)
      .map(el => (el._1, el._2
        .map(edge => if (el._1 == edge.srcId) SemiOutEdge(edge.dstId, edge.attr) else {
          SemiOutEdge(edge.srcId, edge.attr)
        })))

    val initGraph: Graph[SemiVertexData, Double] = conGraph
      .mapVertices((_, _) => Array.empty)
      .outerJoinVertices(edgesBoth)((vid, _, edges) => new SemiVertexData(vid, edges.getOrElse(Array.empty), maxClusterSize, maxCluster, false))

    val initialMassage: List[SemiCluster] = List(SemiCluster(scoreFactor))

    def vertexProgram(vid: VertexId, attr: SemiVertexData, msg: List[SemiCluster]): SemiVertexData = {
      attr.handelIncomingMessage(msg)
    }

    def sendMsg(edge: EdgeTriplet[SemiVertexData, Double]): Iterator[(VertexId, List[SemiCluster])] = {
      val msgSet = mutable.Set.empty[(VertexId, List[SemiCluster])]
      if (!edge.dstAttr.hasNoMoreWork) msgSet.add(edge.dstId, edge.srcAttr.newMsg)
      if (!edge.srcAttr.hasNoMoreWork) msgSet.add(edge.srcId, edge.dstAttr.newMsg)
      msgSet iterator
    }

    def mergeMessage(msg1: List[SemiCluster], msg2: List[SemiCluster]): List[SemiCluster] = msg1.union(msg2).distinct.sorted.take(maxCluster)

    Pregel(initGraph, initialMassage, maxIterations, activeDirection = EdgeDirection.Either)(vertexProgram, sendMsg, mergeMessage)
      .mapVertices((_, attr) => attr.newMsg.take(maxCluster))
      .vertices.flatMap(f => f._2)
      .map(sc => (sc.score, sc.vertexList))
      .filter(f => f._2.nonEmpty)
      .distinct
      .sortBy(c => c._1, ascending = false)
      .collect
      .take(maxCluster)
  }
}