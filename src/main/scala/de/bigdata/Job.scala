package de.bigdata

import de.bigdata.semiClustering.SemiClustering
import org.apache.spark.graphx.{Edge, Graph}
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

    val parser = new scopt.OptionParser[Config]("semi-clustering params") {
      head("semi-clustering params", "1.0")

      opt[Int]('m', "maxiter").action((x, c) =>
        c.copy(maxIter = x)).text("set max iteration")

      opt[Double]('f', "scorefactor").action((x, c) =>
        c.copy(scoreFactor = x)).text("set score factor")

      opt[Int]('x', "topcluster").action((x, c) =>
        c.copy(topXCluster = x)).text("set top X of clusters")

      opt[Int]('c', "clustercapacity").action((x, c) =>
        c.copy(clusterCapacity = x)).text("set capacity of each cluster")

      opt[String]('s', "input").required().action((x, c) =>
        c.copy(input = x)).text("set input path of edge file")
    }

    parser.parse(args, Config()).map { params =>
      run(params)
    }.getOrElse {
      sys.exit(1)
    }

    def run(params: Config) {

      val conf = new SparkConf().setAppName("GraphX - SemiClustering").setMaster("local[*]")
      //    val conf = new SparkConf().setAppName("GraphX - SemiClustering")
      val sc = new SparkContext(conf)

      val file: RDD[String] = sc.textFile(params.input)
      val edgesRDD: RDD[Edge[Double]] = file
        .map {
          line =>
            val field = line.split(",")
            new Edge(field(0).substring(0, field(0).indexOf(".")).toLong,
              field(1).substring(0, field(1).indexOf(".")).toLong, field(2).toDouble)
        }

      val graph: Graph[Int, Double] = Graph.fromEdges(edgesRDD, 1)

      //    val start = System.currentTimeMillis()

      /**
        * (graph, scoreFactor)(maxIter, maxClusterSize, maxCluster)
        */
      SemiClustering(graph, params.scoreFactor)(params.maxIter, params.clusterCapacity, params.topXCluster)

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
}
