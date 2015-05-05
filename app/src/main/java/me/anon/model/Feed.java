package me.anon.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class Feed extends Water
{
	private Nutrient nutrient;
	private double mlpl; // ml per litre
}
