package aldor.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Strings {

	private static final Strings instance = new Strings();
	private final ConcurrentMap<Class<?>, StringCodec<?>> stringCodecForClass = new ConcurrentHashMap<>();


	public static Strings instance() {
		return instance ;
	}

	private Strings() {
		populate();
	}

	private void populate() {
		assert stringCodecForClass.isEmpty();

		stringCodecFor(String.class, new StringCodec<String>() {
			@Override
			public String decode(String to) {
				return to;
			}});
		stringCodecFor(Boolean.class, new StringCodec<Boolean>() {
			@Override
			public Boolean decode(String to) {
				return Boolean.valueOf(to);
			}});
		stringCodecFor(Integer.class, new StringCodec<Integer>() {
			@Override
			public Integer decode(String to) {
				return Integer.valueOf(to);
			}});
	}

	@SuppressWarnings({"unchecked", "ReturnOfInnerClass"})
	public <X> Codec<X, String> stringCodecFor(Class<X> clss) {
		if (Enum.class.isAssignableFrom(clss)) {
			@SuppressWarnings("rawtypes")
			Class<? extends Enum> enumClss = clss.asSubclass(Enum.class);
			return (StringCodec<X>) stringCodecForEnum(enumClss);
		}
		if (!stringCodecForClass.containsKey(clss)) {
			throw new CodecException("Missing " + clss.getName());
		}
		return (Codec<X, String>) stringCodecForClass.get(clss);
	}

	private <X extends Enum<X>> StringCodec<X> stringCodecForEnum(final Class<X> enumClass) {
		final Map<String, X> enumForString = new HashMap<>();
			List<X> constants = Arrays.asList(enumClass.getEnumConstants());
			for (X x: constants) {
				enumForString.put(x.name(), x);
			}
		return new StringCodec<X>() {
			@Override
			public X decode(String txt) {
				X value = enumForString.get(txt);
				if (value == null) {
					//noinspection ProhibitedExceptionThrown
					throw new RuntimeException("Unknown value: " + txt);
				}
				return value;
			}};
	}

	public <T> void stringCodecFor(Class<T> clss, StringCodec<T> codec) {
		stringCodecForClass.put(clss, codec);
	}

	@Nullable
	@Contract("_, null -> null")
	public <S> S decode(@NotNull Class<S> clss, @Nullable String txt) {
		return (txt == null) ? null : stringCodecFor(clss).decode(txt);
	}

	@Nullable
	@Contract("_, null -> null")
	public <S> String encode(@NotNull Class<S> clss, @Nullable S value) {
		return (value == null) ? null : stringCodecFor(clss).encode(value);
	}

	/** Converter between two types; it's expected that null is preserved, and that
	 * decode errors (eg. not a number things) throw an exception.
	 * @author pab
	 *
	 * @param <From>
	 * @param <To>
	 */
	@SuppressWarnings({"InterfaceNamingConvention", "InterfaceWithOnlyOneDirectInheritor"})
	public interface Codec<From, To> {
		public To encode(From from);
		public From decode(To to);
	}

	private abstract static class StringCodec<T> implements Codec<T, String> {
		@Override
		public String encode(T value) {
			return value.toString();
		}
	}

	@SuppressWarnings("serial")
	public static class CodecException extends RuntimeException {
		CodecException(String s, Throwable t) {
			super(s, t);
		}

		public CodecException(String s) {
			super(s);
		}
	}
}
