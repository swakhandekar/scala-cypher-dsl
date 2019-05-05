package com.agrim.scala.cypherDSL.spec

import com.agrim.scala.cypherDSL.spec.implicits.QueryProvider
import shapeless.ops.hlist.ToTraversable
import shapeless.{HList, HNil}

private[spec] sealed abstract class CypherEntity[T <: Product: QueryProvider, H <: HList](
    element: T,
    private val properties: H)(implicit context: Context, i0: ToTraversable.Aux[H, List, Symbol]) {
  private val queryProvider = implicitly[QueryProvider[T]]

  def toQuery: String =
    context
      .map(element)(getIdentifierOnlyQuery)
      .getOrElse {
        val id = context.add(element)
        getQueryBasedOnProperties(id)
      }

  private def getQueryBasedOnProperties(id: String) = {
    val matchers = properties match {
      case _: HNil => queryProvider.getMatchers(element)
      case _       => queryProvider.getMatchers(element, properties)
    }
    makeExpandedQuery(id, matchers)
  }

  private def getIdentifierOnlyQuery(id: String): String = makeQuery(id)

  private def makeExpandedQuery(id: String, parts: Seq[String]) = {
    val repr = s"$id:${element.getClass.getSimpleName} {${parts.mkString(",")}}"
    makeQuery(repr)
  }

  private def makeQuery(repr: String) = s"$repr"
}

private[spec] case class Node[T <: Product: QueryProvider, H <: HList](element: T, private val properties: H)(
    implicit context: Context,
    i0: ToTraversable.Aux[H, List, Symbol])
    extends CypherEntity(element, properties) {
  override def toQuery: String = s"(${super.toQuery})"
}
private[spec] case class Relationship[T <: Product: QueryProvider, H <: HList](element: T, private val properties: H)(
    implicit context: Context,
    i0: ToTraversable.Aux[H, List, Symbol])
    extends CypherEntity(element, properties) {
  override def toQuery: String = s"[${super.toQuery}]"
}
