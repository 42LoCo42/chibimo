package forty.two.chibimo.utils

import kotlin.random.Random

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
fun toCDF(absolutes: List<Int>) = absolutes
	.scan(0, Int::plus).drop(1)

fun <T> toValueCDF(values: List<Pair<T, Int>>) = values
	.map { it.first }
	.zip(toCDF(values.map { it.second }))

fun <T> randomWeightedPrecalculated(values: List<Pair<T, Int>>): T? {
	val random = Random.nextInt(values.last().second)
	return values[values.indexOfFirst { random < it.second }].first
}

fun <T> randomWeighted(values: List<Pair<T, Int>>): T? {
	if(values.isEmpty()) return null
	return randomWeightedPrecalculated(toValueCDF(values))
}