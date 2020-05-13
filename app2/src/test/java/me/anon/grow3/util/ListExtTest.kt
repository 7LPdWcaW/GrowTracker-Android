package me.anon.grow3.util

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be null`
import org.junit.Test

class ListExtTest
{
	@Test
	public fun `test unique by`()
	{
		class test(val type: String)

		val list = listOf(test("a"), test("b"), test("c"), test("a"))
		list.uniqueBy { it.type }
			.size.`should be equal to`(3)
	}

	@Test
	public fun `test last instance of`()
	{
		open class C(open val test: Int)
		class A(override val test: Int): C(test)
		class B(override val test: Int): C(test)

		val list = listOf(A(0), B(0), C(0), A(1), B(2))

		list.lastInstanceOf<A>().`should not be null`()
			.test.`should be equal to`(1)

		list.lastInstanceOf<B>().`should not be null`()
			.test.`should be equal to`(2)

		// will return the last B
		list.lastInstanceOf<C>().`should not be null`()
			.test.`should be equal to`(2)
	}

	@Test
	public fun `test last instance of with params`()
	{
		open class C(open val test: Int)
		class A(override val test: Int): C(test)
		class B(override val test: Int): C(test)

		val list = listOf(A(0), B(0), C(0), A(1), B(2))

		list.lastInstanceOf<A> { it.test == 0 }.`should not be null`()
			.test.`should be equal to`(0)

		list.lastInstanceOf<B> { it.test == 2 }.`should not be null`()
			.test.`should be equal to`(2)

		// will return the last B
		list.lastInstanceOf<C> { it.test == 0 }.`should not be null`()
			.test.`should be equal to`(0)
	}
}
