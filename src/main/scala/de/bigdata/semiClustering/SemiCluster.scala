package de.bigdata.semiClustering

import org.apache.spark.graphx._

/**
  * Klasse repraesentiert ein SemiCluster.
  *
  * @author Jakob Smedegaard Andersen
  */

/**
  * Ein SemiCluster ist eine Ansammlung von Knoten und einem Wert der SemiCluster untereinander vergleichbar macht.
  *
  * @param scoreFactor ein Faktor der bestimmt, wie stark aus dem Cluster herausragende Kanten in die Bewertung des Clusters einbezogen werden sollen.
  * @param vertexList die Liste aller sich in dem Cluster befindenden Knoten.
  * @param semiScore der Wert des Clusters.
  *
  */
private[semiClustering] case class SemiCluster(
                                                private val scoreFactor: Double,
                                                vertexList: List[VertexId] = List.empty,
                                                private val semiScore: SemiScore = new SemiScore) extends Ordered[SemiCluster] with Serializable {

  /**
    * Gibt die Anzahl an Knoten im Cluster zurueck.
    *
    * @return die Anzahl an Knoten im Cluster.
    */
  def size: PartitionID = vertexList.size

  /**
    * Gibt den Wert des Clusters als Double zurueck.
    *
    * @return der Wert des Clusters.
    */
  def score: Double = semiScore.score

  /**
    * Fuegt einen weiteren Knoten in das Cluster hinzu, wenn dieser noch nicht Bestandteil dieses ist.
    *
    * @param vid die Identifizierung des Knotens.
    * @param outgoingEdges die aus diesem Knoten ausgehenden Kanten.
    *
    * @return ein um den neuen Knoten erweitertes oder unveraendertes Cluster
    */
  def addVertex(vid: VertexId, outgoingEdges: Array[SemiOutEdge]): SemiCluster = {
    val newVertexList = this.vertexList.::(vid).sorted
    SemiCluster(scoreFactor, newVertexList, this.semiScore.recalculate(newVertexList, outgoingEdges, scoreFactor))
  }

  override def compare(that: SemiCluster): Int = {
    this.semiScore.compare(that.semiScore)
  }

  override def toString: String = {
    "{" + vertexList + "#" + score + "}"
  }

}