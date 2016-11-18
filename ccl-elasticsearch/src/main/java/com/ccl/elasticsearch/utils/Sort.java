package com.ccl.elasticsearch.utils;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * User: dean.lu Date: 13-9-6 Time: 下午5:15
 */
@Label("排序")
public class Sort extends AbstractList<Sort.Order> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 6092635739350855047L;

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;

    @NotEmpty
    @Valid
    @Label("排序项列表")
    private List<Order> orderList;

    public Sort() {
        orderList = new ArrayList<>();
    }

    @Override
    public void add(int index, Order element) {
        orderList.add(index, element);
    }

    @Override
    public Order get(int index) {
        return orderList.get(index);
    }

    @Override
    public int size() {
        return orderList.size();
    }

    @Override
    public Iterator<Order> iterator() {
        return orderList.iterator();
    }

    @Override
    public Order remove(int index) {
        return orderList.remove(index);
    }

    @Override
    public String toString() {
        return orderList.toString();
    }

    @Label("排序项")
    public static class Order implements Serializable {

        private static final long serialVersionUID = 1522511010900108987L;

        @Label("方向")
        private Direction direction;
        @NotNull
        @Label("属性")
        private String property;

        public Order() {
            this.direction = DEFAULT_DIRECTION;
        }

        public Order(Direction direction, String property) {

            if (null == property || "".equals(property)) {
                throw new IllegalArgumentException(
                        "Property must not null or empty!");
            }

            this.direction = direction == null ? DEFAULT_DIRECTION : direction;
            this.property = property;
        }

        public Order(String property) {
            this(DEFAULT_DIRECTION, property);
        }

        /**
         * Returns the order the property shall be sorted for.
         *
         * @return
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * Returns the property to order for.
         *
         * @return
         */
        public String getProperty() {
            return property;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int result = 17;

            result = 31 * result + direction.hashCode();
            result = 31 * result + property.hashCode();

            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Order)) {
                return false;
            }

            Order that = (Order) obj;

            return this.direction.equals(that.direction)
                    && this.property.equals(that.property);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s: %s", property, direction);
        }
    }

    /**
     * Enumeration for sort directions.
     *
     * @author Oliver Gierke
     */
    public static enum Direction {

        ASC, DESC;

        /**
         * Returns the {@link Direction} enum for the given {@link String}
         * value.
         *
         * @param value
         * @return
         */
        public static Direction fromString(String value) {

            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).",
                                value), e);
            }
        }
    }
}
