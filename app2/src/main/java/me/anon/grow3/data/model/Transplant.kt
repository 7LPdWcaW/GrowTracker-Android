package me.anon.grow3.data.model

typealias Medium = Transplant

class Transplant(
	public var medium: MediumType,
	public var size: Double?
) : Log(action = "Transplant")
