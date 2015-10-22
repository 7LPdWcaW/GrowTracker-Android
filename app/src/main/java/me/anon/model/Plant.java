package me.anon.model;

import java.util.ArrayList;

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
@Getter @Setter
@Accessors(prefix = {"m", ""}, chain = true)
public class Plant
{
	private String name;
	private String strain;
	private long plantDate = System.currentTimeMillis();
	private boolean clone = false;
	private PlantStage stage = PlantStage.GERMINATION;
	private PlantMedium medium = PlantMedium.SOIL;
	private ArrayList<String> images = new ArrayList<>();
	private ArrayList<Action> actions = new ArrayList<>();
}
