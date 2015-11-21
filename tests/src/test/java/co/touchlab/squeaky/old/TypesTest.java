package co.touchlab.squeaky.old;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by kgalligan on 7/19/15.
 */
@RunWith(RobolectricTestRunner.class)
public class TypesTest extends BaseTest
{
	@Test
	public void basicDbTest() throws SQLException
	{
		SimpleHelper helper = createHelper(ManyTypes.class);
		Dao<ManyTypes> dao = helper.getDao(ManyTypes.class);

		ManyTypes manyTypes = new ManyTypes();
		manyTypes.id = 1;
		manyTypes.b1 = true;
		manyTypes.b2 = false;
		manyTypes.by1 = 3;
		manyTypes.by2 = 5;
//		manyTypes.bytes = new byte[]{3, 2, 4, 1, 0, 6};
		manyTypes.d1 = 423d;
		manyTypes.d2 = 6662d;
		manyTypes.dateDefault = new Date();
		manyTypes.dateLong = new Date();
		manyTypes.dateYYMMDD = new Date();
		manyTypes.f1 = 234f;
		manyTypes.f2 = 6662f;
		manyTypes.i1 = 48928374;
		manyTypes.i2 = 48418374;
		manyTypes.l1 = 8987189312l;
		manyTypes.l2 = 18987189312l;
		manyTypes.s1 = 31;
		manyTypes.s2 = 234;
		manyTypes.st1 = "Heyo lots of stuff";
		dao.create(manyTypes);

		List<ManyTypes> bs = dao.queryForAll().list();

		Assert.assertEquals(manyTypes, bs.get(0));

		helper.close();
	}

	@DatabaseTable
	static class ManyTypes
	{
		public static final String MM_DD_YYYY = "MM/dd/yyyy";
		@DatabaseField(id = true)
		public int id;
		@DatabaseField
		public boolean b1;
		@DatabaseField
		public Boolean b2;
		@DatabaseField
		public short s1;
		@DatabaseField
		public Short s2;
		@DatabaseField
		public byte by1;
		@DatabaseField
		public Byte by2;
		@DatabaseField
		public int i1;
		@DatabaseField
		public Integer i2;
		@DatabaseField
		public long l1;
		@DatabaseField
		public Long l2;
		@DatabaseField
		public float f1;
		@DatabaseField
		public Float f2;
		@DatabaseField
		public double d1;
		@DatabaseField
		public Double d2;

		@DatabaseField
		public String st1;
		@DatabaseField
		public Date dateDefault;
		@DatabaseField(dataType = DataType.DATE_LONG)
		public Date dateLong;

		@DatabaseField(format = MM_DD_YYYY)
		public Date dateYYMMDD;

		private DateFormat checkFormat = new SimpleDateFormat(MM_DD_YYYY);

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ManyTypes manyTypes = (ManyTypes) o;

			if (id != manyTypes.id) return false;
			if (b1 != manyTypes.b1) return false;
			if (s1 != manyTypes.s1) return false;
			if (by1 != manyTypes.by1) return false;
			if (i1 != manyTypes.i1) return false;
			if (l1 != manyTypes.l1) return false;
			if (Float.compare(manyTypes.f1, f1) != 0) return false;
			if (Double.compare(manyTypes.d1, d1) != 0) return false;
			if (b2 != null ? !b2.equals(manyTypes.b2) : manyTypes.b2 != null) return false;
			if (s2 != null ? !s2.equals(manyTypes.s2) : manyTypes.s2 != null) return false;
			if (by2 != null ? !by2.equals(manyTypes.by2) : manyTypes.by2 != null) return false;
			if (i2 != null ? !i2.equals(manyTypes.i2) : manyTypes.i2 != null) return false;
			if (l2 != null ? !l2.equals(manyTypes.l2) : manyTypes.l2 != null) return false;
			if (f2 != null ? !f2.equals(manyTypes.f2) : manyTypes.f2 != null) return false;
			if (d2 != null ? !d2.equals(manyTypes.d2) : manyTypes.d2 != null) return false;
			if (st1 != null ? !st1.equals(manyTypes.st1) : manyTypes.st1 != null) return false;
			if (dateDefault != null ? !dateDefault.equals(manyTypes.dateDefault) : manyTypes.dateDefault != null)
				return false;
			if (dateLong != null ? !dateLong.equals(manyTypes.dateLong) : manyTypes.dateLong != null) return false;
//			if (!Arrays.equals(bytes, manyTypes.bytes)) return false;
			return !(dateYYMMDD != null ? !checkFormat.format(dateYYMMDD).equals(checkFormat.format(manyTypes.dateYYMMDD)) : manyTypes.dateYYMMDD != null);

		}

		@Override
		public int hashCode()
		{
			int result;
			long temp;
			result = id;
			result = 31 * result + (b1 ? 1 : 0);
			result = 31 * result + (b2 != null ? b2.hashCode() : 0);
			result = 31 * result + (int) s1;
			result = 31 * result + (s2 != null ? s2.hashCode() : 0);
			result = 31 * result + (int) by1;
			result = 31 * result + (by2 != null ? by2.hashCode() : 0);
			result = 31 * result + i1;
			result = 31 * result + (i2 != null ? i2.hashCode() : 0);
			result = 31 * result + (int) (l1 ^ (l1 >>> 32));
			result = 31 * result + (l2 != null ? l2.hashCode() : 0);
			result = 31 * result + (f1 != +0.0f ? Float.floatToIntBits(f1) : 0);
			result = 31 * result + (f2 != null ? f2.hashCode() : 0);
			temp = Double.doubleToLongBits(d1);
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			result = 31 * result + (d2 != null ? d2.hashCode() : 0);
			result = 31 * result + (st1 != null ? st1.hashCode() : 0);
			result = 31 * result + (dateDefault != null ? dateDefault.hashCode() : 0);
			result = 31 * result + (dateLong != null ? dateLong.hashCode() : 0);
			result = 31 * result + (dateYYMMDD != null ? checkFormat.format(dateYYMMDD).hashCode() : 0);
//			result = 31 * result + (bytes != null ? Arrays.hashCode(bytes) : 0);
			return result;
		}

		@Override
		public String toString()
		{
			return "ManyTypes{" +
					"id=" + id +
					", b1=" + b1 +
					", b2=" + b2 +
					", s1=" + s1 +
					", s2=" + s2 +
					", by1=" + by1 +
					", by2=" + by2 +
					", i1=" + i1 +
					", i2=" + i2 +
					", l1=" + l1 +
					", l2=" + l2 +
					", f1=" + f1 +
					", f2=" + f2 +
					", d1=" + d1 +
					", d2=" + d2 +
					", st1='" + st1 + '\'' +
					", dateDefault=" + dateDefault +
					", dateLong=" + dateLong +
					", dateYYMMDD=" + dateYYMMDD +
//					", bytes=" + Arrays.toString(bytes) +
					", checkFormat=" + checkFormat +
					'}';
		}
	}
}
