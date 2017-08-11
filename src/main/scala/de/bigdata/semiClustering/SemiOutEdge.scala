package de.bigdata.semiClustering

/**
  * Klasse repraesentiert eine ausgehende Kante.
  *
  * @author Jakob Smedegaard Andersen
  */

/**
  * Eine ausgehende Kante besteht aus der Identifikation seines Endknotens und einem Attribut.
  *
  * @param id die Identifikation des Endknotens der ausgehenden Kante.
  * @param attr das mit der ausgehenden Kante assoziierte Attribut.
  */
private[semiClustering] case class SemiOutEdge(
                                                id: Long,
                                                attr: Double) extends Serializable
