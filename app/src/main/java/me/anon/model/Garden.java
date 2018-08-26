package me.anon.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter
@Accessors(prefix = {"m", ""}, chain = true)
@NoArgsConstructor
public class Garden
{
	protected String name;
	protected ArrayList<String> plantIds;
}
