package de.bigdata.semiClustering

import scala.annotation.tailrec

/**
  * Utility Methoden fuer das Semi-Clustering
  *
  * @author Jakob
  */
object SemiUtility {

  /**
    * Prueft ob alle Elemente einer Liste in einer anderen enthalten sind.
    *
    * @param sub  die zu ueberpruefende Untermenge.
    * @param list die Grundmenge.
    *
    */
  @tailrec def isSubSet(sub: List[_], list: List[_]): Boolean = sub match {
    case Nil => true
    case hd :: tail => list.contains(hd) && isSubSet(tail, list)
  }

}