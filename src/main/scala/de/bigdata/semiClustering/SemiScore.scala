package de.bigdata.semiClustering

import org.apache.spark.graphx._

/**
  * Klasse repraesentiert den Wert eines SemiClusters.
  *
  * @author Jakob Smedegaard Andersen
  */

/**
  * Ein SemiScore gibt den aktuellen Wert eines SemiClusters wider.
  *
  * @param weightInnerEdges   die Summe der Gewichtungen der Kanten,
  *                           fuer die sowohl der Anfangs- oder Endknoten Bestandteil von c sind.
  * @param weightBoundetEdges die Summe der Gewichtungen der Kanten,
  *                           fuer die entweder der Anfangs- oder Endknoten nicht Bestandteil von c ist.
  * @param score              der Wert des Clusters
  *
  */
case class SemiScore(
                      private val weightInnerEdges: Double = 0,
                      private val weightBoundetEdges: Double = 0,
                      score: Double = 0) extends Ordered[SemiScore] with Serializable {

  /**
    * Berechnung eines neuen Clusterwertes.
    *
    * @param vertexList    Liste der Knoten die das Cluster umfasst.
    * @param outGoingEdges die mit dem zuletzt hinzugefuegten Knoten assoziierten ausgehenden Kanten.
    * @param scorFactor   ein Faktor der bestimmt, wie stark aus dem Cluster herausragende Kanten in die Bewertung des Clusters einbezogen werden sollen.
    *
    */
  def recalculate(vertexList: List[VertexId], outGoingEdges: Array[SemiOutEdge], scorFactor: Double): SemiScore = {
    if (vertexList.size == 1) {
      SemiScore(weightInnerEdges, outGoingEdges.aggregate(0d)(_ + _.attr, _ + _), 1)
    } else {
      var wbe: Double = weightBoundetEdges
      var wie: Double = weightInnerEdges
      for (edge <- outGoingEdges) {
        if (vertexList.contains(edge.id)) {
          wbe -= edge.attr
          wie += edge.attr
        } else {
          wbe += edge.attr
        }
      }
      SemiScore(wie, wbe, (wie - scorFactor * wbe) / ((vertexList.size * (vertexList.size - 1)) >> 1))
    }
  }

  override def compare(that: SemiScore): Int = {
    if (this.score < that.score) 1 else if (this.score > that.score) -1 else 0
  }
}