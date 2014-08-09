package wbs.sms.core.hibernate;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import wbs.sms.core.model.SmsOrig;
import wbs.sms.core.model.SmsOrigType;

public
class SmsOrigHibernateType
	implements CompositeUserType {

	@Override
	public
	String[] getPropertyNames () {

		return new String [] {
			"type",
			"value"
		};

	}

	@Override
	public
	Type[] getPropertyTypes () {

		return new Type [] {
			SmsOrigTypeHibernateType.INSTANCE,
			StandardBasicTypes.STRING
		};

	}

	@Override
	public
	Object getPropertyValue (
			Object component,
			int property)
		throws HibernateException {

		SmsOrig smsOrig =
			(SmsOrig) component;

		switch (property) {

		case 0:
			return smsOrig.type ();

		case 1:
			return smsOrig.value ();

		}

		throw new HibernateException (
			"Invalid property " + property);

	}

	@Override
	public
	void setPropertyValue (
			Object component,
			int property,
			Object value)
		throws HibernateException {

		throw new HibernateException (
			"Immutable");

	}

	@Override
	public
	Class<?> returnedClass () {
		return SmsOrig.class;
	}

	@Override
	public
	boolean equals (
			Object left,
			Object right)
		throws HibernateException {

		if (left == right)
			return true;

		if (left == null || right == null)
			return false;

		SmsOrig smsOriginatorLeft =
			(SmsOrig) left;

		SmsOrig smsOriginatorRight =
			(SmsOrig) right;

		return
			equal (
				smsOriginatorLeft.type (),
				smsOriginatorRight.type ())
			&& equal (
				smsOriginatorLeft.value (),
				smsOriginatorRight.value ());

	}

	@Override
	public
	int hashCode (
			Object x)
		throws HibernateException {

		return x.hashCode ();

	}

	@Override
	public
	Object nullSafeGet (
			ResultSet result,
			String[] names,
			SessionImplementor session,
			Object owner)
		throws HibernateException,
			SQLException {

		SmsOrigType type = (SmsOrigType)
			SmsOrigTypeHibernateType.INSTANCE.nullSafeGet (
				result,
				new String[] { names[0] },
				session,
				null);

		String value = (String)
			StandardBasicTypes.STRING.nullSafeGet (
				result,
				names [1],
				session);

		if (type == null || value == null)
			return null;
		return new SmsOrig(type, value);
	}

	@Override
	public
	void nullSafeSet (
			PreparedStatement preparedStatement,
			Object object,
			int index,
			SessionImplementor session)
		throws HibernateException,
			SQLException {

		SmsOrig smsOrig =
			(SmsOrig) object;

		SmsOrigType type =
			smsOrig != null
				? smsOrig.type ()
				: null;

		String value =
			smsOrig != null
				? smsOrig.value ()
				: null;

		SmsOrigTypeHibernateType.INSTANCE.nullSafeSet (
			preparedStatement,
			type,
			index ++,
			session);

		StandardBasicTypes.STRING.nullSafeSet (
			preparedStatement,
			value,
			index ++,
			session);

	}

	@Override
	public
	Object deepCopy (
			Object value)
		throws HibernateException {

		return value;

	}

	@Override
	public
	boolean isMutable () {
		return false;
	}

	@Override
	public
	Serializable disassemble (
			Object value,
			SessionImplementor session)
		throws HibernateException {

		return (Serializable) value;

	}

	@Override
	public
	Object assemble (
			Serializable cached,
			SessionImplementor session,
			Object object)
		throws HibernateException {

		return cached;
	}

	@Override
	public
	Object replace (
			Object original,
			Object target,
			SessionImplementor session,
			Object owner)
		throws HibernateException {

		return original;

	}

}
