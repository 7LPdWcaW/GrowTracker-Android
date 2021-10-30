package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Environment
import me.anon.grow3.databinding.CardEnvironmentLogBinding
import me.anon.grow3.util.string

class EnvironmentLogCard : LogCard<CardEnvironmentLogBinding, Environment>
{
	constructor() : super()
	constructor(diary: Diary, log: Environment) : super(diary, log)

	inner class EnvironmentLogCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= EnvironmentLogCardHolder(CardEnvironmentLogBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardEnvironmentLogBinding = CardEnvironmentLogBinding.bind(view)

	override fun bindLog(view: CardEnvironmentLogBinding)
	{
		var stringParts = arrayListOf<String>()


		log.type?.let {
			stringParts += it.strRes.string()
		}

		log.size?.let {
			val stringBuilder = StringBuilder()

			it.width?.let {
				stringBuilder
					.append("W: ")
					.append(it.amount)
					.append(it.unit.strRes.string())
					.append(", ")
			}

			it.height?.let {
				stringBuilder
					.append("H: ")
					.append(it.amount)
					.append(it.unit.strRes.string())
					.append(", ")
			}

			it.depth?.let {
				stringBuilder
					.append("D: ")
					.append(it.amount)
					.append(it.unit.strRes.string())
					.append(", ")
			}

			stringBuilder.removeSuffix(", ").toString()
		}

		log.light?.let {
			val stringBuilder = StringBuilder()

			stringBuilder
				.append(it.type.strRes.string())
				.append(" ")

			it.brand?.let { brand ->
				stringBuilder
					.append(brand)
					.append(" ")
			}

			it.wattage?.let { wattage ->
				stringBuilder
					.append(wattage)
					.append("w")
			}

			stringBuilder.append(", ")
			stringParts += stringBuilder.removeSuffix(", ").toString()
		}

		view.content.text = stringParts.joinToString("\n")
	}
}
