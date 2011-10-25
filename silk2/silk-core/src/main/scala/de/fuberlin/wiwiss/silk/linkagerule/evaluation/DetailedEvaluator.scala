package de.fuberlin.wiwiss.silk.linkagerule.evaluation

import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.linkagerule.similarity.{Comparison, Aggregation, SimilarityOperator}
import de.fuberlin.wiwiss.silk.linkagerule.input.{TransformInput, PathInput, Input}
import de.fuberlin.wiwiss.silk.linkagerule.LinkageRule
import de.fuberlin.wiwiss.silk.entity.Entity
import de.fuberlin.wiwiss.silk.linkagerule.evaluation.DetailedLink._

object DetailedEvaluator {
  def apply(condition: LinkageRule, entities: DPair[Entity], limit: Double = -1.0): Option[DetailedLink] = {
    condition.operator match {
      case Some(op) => {
        val confidence = evaluateOperator(op, entities, limit)

        if (confidence.value.getOrElse(-1.0) >= limit)
          Some(new DetailedLink(entities.source.uri, entities.target.uri, Some(entities), Some(confidence)))
        else
          None
      }
      case None => {
        if (limit == -1.0)
          Some(new DetailedLink(entities.source.uri, entities.target.uri, Some(entities), Some(SimpleConfidence(Some(-1.0)))))
        else
          None
      }
    }
  }

  private def evaluateOperator(operator: SimilarityOperator, entities: DPair[Entity], threshold: Double) = operator match {
    case aggregation: Aggregation => evaluateAggregation(aggregation, entities, threshold)
    case comparison: Comparison => evaluateComparison(comparison, entities, threshold)
  }

  private def evaluateAggregation(agg: Aggregation, entities: DPair[Entity], threshold: Double): DetailedLink.AggregatorConfidence = {
    val totalWeights = agg.operators.map(_.weight).sum

    var isNone = false

    val operatorValues = {
      for (operator <- agg.operators) yield {
        val updatedThreshold = agg.aggregator.computeThreshold(threshold, operator.weight.toDouble / totalWeights)
        val value = evaluateOperator(operator, entities, updatedThreshold)
        if (operator.required && value.value.isEmpty) isNone = true

        value
      }
    }

    val weightedValues = for((weight, Some(value)) <- agg.operators.map(_.weight) zip operatorValues.map(_.value)) yield (weight, value)

    val aggregatedValue = agg.aggregator.evaluate(weightedValues)

    if (isNone)
      AggregatorConfidence(None, agg, operatorValues)
    else
      AggregatorConfidence(aggregatedValue, agg, operatorValues)
  }

  private def evaluateComparison(comparison: Comparison, entities: DPair[Entity], threshold: Double): DetailedLink.ComparisonConfidence = {
    val distance = comparison.apply(entities, threshold)

    val sourceInput = findInput(comparison.inputs.source)
    val targetInput = findInput(comparison.inputs.target)

    val sourceValue = InputValue(sourceInput, comparison.inputs.source(entities))
    val targetValue = InputValue(targetInput, comparison.inputs.target(entities))

    ComparisonConfidence(distance, comparison, sourceValue, targetValue)
  }

  private def findInput(input: Input): PathInput = input match {
    case input: PathInput => input
    case TransformInput(_, _, inputs) => findInput(inputs.head)
  }
}