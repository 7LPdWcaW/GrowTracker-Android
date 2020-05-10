package me.anon.grow3.data.repository

import me.anon.grow3.util.parseAsGardens

/**
 * // TODO: Add class description
 */
object TestConstants
{
	public const val gardens_json = """
		[
			{
				"id": "abcd-efgh-1234-567890",
				"name": "Garden 1"
			}
		]
	"""

	public val gardens by lazy {
		gardens_json.parseAsGardens()
	}
}
