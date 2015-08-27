package com.kumasuke.fetcher.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * 工具类，提供系列工具方法。
 */
public class Tools {
    // 工具类，防止被创建
    private Tools() {
        throw new AssertionError();
    }

    /**
     * 返回集合类是否非 null 且非空。
     *
     * @param collection 集合类
     * @return 是否非 null 且非空
     */
    public static boolean nonNullAndNonEmpty(Collection collection) {
        return !isNullOrEmpty(collection);
    }

    /**
     * 返回字符序列类是否非 null 且非空。
     *
     * @param charSequence 字符序列类
     * @return 是否非 null 且非空
     */
    public static boolean nonNullAndNonEmpty(CharSequence charSequence) {
        return !isNullOrEmpty(charSequence);
    }

    /**
     * 返回集合类是否为 null 或空。
     *
     * @param collection 集合类
     * @return 是否为 null 或空
     */
    public static boolean isNullOrEmpty(Collection collection) {
        return isNull(collection) || collection.isEmpty();
    }

    /**
     * 返回字符序列类是否为 null 或空。
     *
     * @param charSequence 字符序列类
     * @return 是否为 null 或空
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return isNull(charSequence) || charSequence.length() == 0;
    }

    /**
     * 将一个或多个对象转换为一个 {@code Set} 对象，同时使用给定的映射转换对象。
     *
     * @param mapper 指定映射
     * @param args   需要转换的对象
     * @param <T>    输入参数类型
     * @param <R>    返回集合内部值类型
     * @return 装有映射后的对象的 {@code Set} 对象
     */
    @SafeVarargs
    public static <T, R> Set<R> toSet(Function<T, R> mapper, T... args) {
        return Stream.of(args)
                .map(mapper)
                .collect(Collectors.toSet());
    }

    /**
     * 生成一个键值对，一旦创建完成，无法修改。
     *
     * @param key   {@code key} 值
     * @param value {@code value} 值
     * @param <K>   key 值类型
     * @param <V>   value 值类型
     * @return 生成的键值对，可作为 {@link Tools#toMap(P[]) toMap(P&lt;K, V&gt;...)} 的参数
     */
    public static <K, V> P<K, V> p(K key, V value) {
        return new P<>(key, value);
    }

    /**
     * 将一个或多个键值对转换为一个 {@code Map} 对象。
     *
     * @param args 需要转换的键值对，由 {@link Tools#p(Object, Object) p(K, V)} 方法生成
     * @param <K>  key 值类型
     * @param <V>  value 值类型
     * @return 装有传入的键值对的 {@code Map} 对象
     */
    @SafeVarargs
    public static <K, V> Map<K, V> toMap(P<K, V>... args) {
        return Stream.of(args)
                .collect(Collectors.toMap(P::getKey, P::getValue));
    }

    /**
     * 临时键值对，外部仅可使用 {@link Tools#p(Object, Object) Tools.p(K, V)} 方法创建。<br>
     * 一旦创建完成，无法修改。
     *
     * @param <K> key 值类型
     * @param <V> value 值类型
     */
    public static class P<K, V> {
        private final K key;
        private final V value;

        private P(K key, V value) {
            this.key = requireNonNull(key, "The 'key' must not be null.");
            this.value = value;
        }

        /**
         * 获取 key 值。
         *
         * @return key 值
         */
        public K getKey() {
            return key;
        }

        /**
         * 获取 value 值。
         *
         * @return value 值
         */
        public V getValue() {
            return value;
        }
    }
}
