package de.bigdata.semiClustering

/**
  * Klasse repraesentiert den Typ der Property eines Knotens.
  *
  * @author Jakob Smedegaard Andersen
  */

/**
  * Objekte der Klasse beinhalten saemtliche Informationen, auf die innerhalb eines Supersteps zugegriffen werden soll.
  *
  * @param vertex die Identifizierung des jeweiligen Knotens.
  * @param outGoingEdges die Menge der von dem Knoten ausgehenden Kanten.
  * @param maxClusterSize die Maximalgroesse des Clusters.
  * @param maxCluster die maximale Anzahl an Clustern.
  * @param hasNoMoreWork die Bedingung ob ein Knoten in den Zustand passiv uebergehen kann.
  * @param clusterList die Liste aller SemiCluster fuer den Knoten.
  * @param newMsg die Nachricht, die als naechstes an andere Knoten verschickt werden soll.
  */
private[semiClustering] class SemiVertexData(
                                              private val vertex: Long,
                                              private val outGoingEdges: Array[SemiOutEdge],
                                              private val maxClusterSize: Int,
                                              private val maxCluster: Int,
                                              val hasNoMoreWork: Boolean,
                                              val clusterList: List[SemiCluster] = List.empty,
                                              val newMsg: List[SemiCluster] = List.empty) extends Serializable {

  /**
    * Objekte der Klasse beinhalten saemtliche Informationen, auf die innerhalb eines Supersteps zugegriffen werden soll.
    *
    * Kopiert alle Werte eines bestehenden SemiVertexData Objekts, welche sich nicht ueber Supersteps hinweg veraendern.
    * Dient der Uebersichtlichkeit.
    *
    * @param other eine anderes SemiVertexData Object, dessen Werte fuer:
    * vertex, outGoingEdges, maxClusterSize, maxCluster uebernommen werden.
    *
    * @param hasNoMoreWork die Bedingung ob ein Knoten in dem Zustand passiv uebegehen kann.
    * @param clusterList die Liste aller SemiCluster fuer den Knoten.
    * @param newMsg die Nachricht, die als naechstes an andere Knoten verschickt werden soll.
    */
  def this(other: SemiVertexData)(hasNoMoreWork: Boolean, clusterList: List[SemiCluster], newMsg: List[SemiCluster]) = {
    this(other.vertex, other.outGoingEdges, other.maxClusterSize, other.maxCluster, hasNoMoreWork, clusterList, newMsg)
  }

  /**
    * Reaktion auf eingehende Nachrichten.
    *
    * @param msg die dem Knoten geschickte Liste von SemiClustern.
    *
    * @return die neue Property des Knotens.
    */

  def handelIncomingMessage(msg: List[SemiCluster]): SemiVertexData = {
    val oldClusterList = this.clusterList
    val newAttr = msg.filter(c => c.size < this.maxClusterSize && !c.vertexList.contains(this.vertex))
      .map(_.addVertex(this.vertex, outGoingEdges))
    val newClusterList = newAttr.union(this.clusterList)
      .distinct
      .sorted
    val msgToSend = newClusterList.union(msg)
      .distinct
      .take(maxCluster)
    new SemiVertexData(this)(
      hasNoMoreWork = SemiUtility.isSubSet(newAttr, oldClusterList),
      newClusterList,
      msgToSend)
  }
}
