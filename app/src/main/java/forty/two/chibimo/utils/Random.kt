package forty.two.chibimo.utils

import kotlin.random.Random

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */

/**
 * Convert [absolutes] to the cumulative distribution form.
 * Example: [1, 3, 3, 2, 4, 1] -> [1, 4, 7, 9, 13, 14]
 */
fun toCDF(absolutes: List<Int>) = absolutes
	.scan(0, Int::plus).drop(1)

/**
 * Like [toCDF] but for lists of value-weight pairs.
 */
fun <T> toValueCDF(values: List<Pair<T, Int>>) = values
	.map { it.first }
	.zip(toCDF(values.map { it.second }))

/**
 * Accept a precalculated value-CDF list and return a weighted random element.
 * Generate a suitable list with [toValueCDF].
 */
fun <T> randomWeightedPrecalculated(values: List<Pair<T, Int>>): T? {
	val random = Random.nextInt(values.last().second)
	return values[values.indexOfFirst { random < it.second }].first
}

/**
 * Return a weighted random element of the supplied list.
 */
fun <T> randomWeighted(values: List<Pair<T, Int>>): T? {
	if(values.isEmpty()) return null
	return randomWeightedPrecalculated(toValueCDF(values))
}