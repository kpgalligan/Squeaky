package com.j256.ormlite.android.apptools;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by kgalligan on 7/18/15.
 */
@DatabaseTable
public class BasicEntity
{
	@DatabaseField(generatedId = true)
	public Long id;

	@DatabaseField
	public String val;
}
