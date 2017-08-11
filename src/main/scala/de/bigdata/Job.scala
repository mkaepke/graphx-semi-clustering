package de.bigdata

import java.text.SimpleDateFormat
import java.util.Date

import de.bigdata.semiClustering.SemiClustering
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Anwendung des Semi-Clustering Algorithmus unter Spark mit Hilfe von GraphX.
  * Jakob Smedegaard Andersen ist Autor der Algorithmus-Implementierung.
  *
  * @author Marc Kaepke
  */
object Job {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("GraphX - SemiClustering").setMaster("local[*]")
//    val conf = new SparkConf().setAppName("GraphX - PageRank as vertex-centric")
    val sc = new SparkContext(conf)

    val file: RDD[String] = sc.textFile("/Users/marc/Downloads/zz_THESIS_zz/edges.csv")
    val edgesRDD: RDD[Edge[Double]] = file
        .map {
          line => val field = line.split(",")
            new Edge(field(0).substring(0, field(0).indexOf(".")).toLong, field(1).substring(0, field(1).indexOf(".")).toLong, field(2).toDouble)
        }

    val graph: Graph[Int, Double] = Graph.fromEdges(edgesRDD, 1)

//    val start = System.currentTimeMillis()

    /**
      * (graph, scoreFactor)(maxIter, maxClusterSize, maxCluster)
      */
    val result: Array[(Double, List[VertexId])] = SemiClustering(graph, 0.5)(10, 2, 3)

//    println("Dauer vom SemiClustering (ohne Graph-Erzeugung): " + (System.currentTimeMillis() - start) + "ms")

//    for(i <- result.indices){
//      println("i is: " + i)
//      println("i'th element is: " + result(i))
//    }


//    val requestedNumVertices = args(0).toInt
//    val numEdges = args(1).toInt
//    val powergraph = GraphGenerators.rmatGraph(sc, requestedNumVertices, numEdges)

//    val dateFormatter = new SimpleDateFormat("yyyy:MM:dd_hh:mm:ss")
//    val submittedDateConvert = new Date()
//    val submittedAt = dateFormatter.format(submittedDateConvert)

//    powergraph.edges.map(e => e.srcId + "," + e.dstId).repartition(1).saveAsTextFile("/output/" + submittedAt + "/edges" + powergraph.numEdges + "/")
  }
}
