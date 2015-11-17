package co.touchlab.squeaky.old;

import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;

/**
 * Created by kgalligan on 7/18/15.
 */
@DatabaseTable
public class A
{
	@DatabaseField(generatedId = true)
	public Long id;

	@DatabaseField
	public String name;

	@DatabaseField
	public int a;

	@DatabaseField
	public long b;

	@DatabaseField
	public float c;

	@DatabaseField
	public double d;

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		A a1 = (A) o;

		if (a != a1.a) return false;
		if (b != a1.b) return false;
		if (Float.compare(a1.c, c) != 0) return false;
		if (Double.compare(a1.d, d) != 0) return false;
		if (id != null ? !id.equals(a1.id) : a1.id != null) return false;
		return !(name != null ? !name.equals(a1.name) : a1.name != null);

	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + a;
		result = 31 * result + (int) (b ^ (b >>> 32));
		result = 31 * result + (c != +0.0f ? Float.floatToIntBits(c) : 0);
		temp = Double.doubleToLongBits(d);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
