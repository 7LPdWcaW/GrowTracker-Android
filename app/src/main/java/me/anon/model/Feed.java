package me.anon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Accessors(prefix = {"m", ""}, chain = true)
@Getter @Setter
public class Feed extends Water
{
	private Nutrient nutrient;
	private Double mlpl; // ml per litre
}
