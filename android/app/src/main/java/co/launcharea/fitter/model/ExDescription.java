package co.launcharea.fitter.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jack on 15. 8. 20.
 */
public class ExDescription {
    public static final int TYPE_TIME = 2;
    static final int PROTOCOL_VERSION = 1;
    static final int TYPE_WEIGHT = 1;
    static final int TYPE_ETC = 3;
    private final String mObjectId;
    private final int mVersion;
    List<Item> items = new LinkedList<Item>();

    public ExDescription(String objectId, int version) {
        mObjectId = objectId;
        mVersion = version;
    }

    public void add(Item item) {
        items.add(item);
    }

    public int numItems() {
        return items.size();
    }

    public void loadItemFromJson(String colName, String data) throws JSONException {
        if (data == null || data.length() == 0) {
            return;
        }

        if (mVersion != PROTOCOL_VERSION) {
            return;
        }

        JSONObject json = new JSONObject(data);
        Item item = new Item(colName, json.getInt("type"), json.getString("name").toUpperCase());
        item.min = json.optDouble("min", 0.0f);
        item.max = json.optDouble("max", 0.0f);
        item.diff = json.optDouble("diff", 0.0f);
        item.defaultValue = json.optDouble("default");
        item.operator = json.optString("operator", "").toLowerCase();
        item.label = json.optString("label");
        items.add(item);
    }

    public String getObjectId() {
        return mObjectId;
    }

    public String getDescription() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            Item each = items.get(i);
            builder.append(each.name);
            if (each.type == TYPE_WEIGHT) {
                builder.append("(" + each.diff + ")");
            }
            if (each.operator != null && each.operator.length() > 0) {
                builder.append(" " + each.operator + " ");
            } else {
                if (i != items.size() - 1)
                    builder.append(", ");
            }
        }
        return builder.toString();
    }

    public Item getItem(int i) {
        return items.get(i);
    }

    public String toString(double data1, double data2, double data3, double data4) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            double value = data1;
            switch (i) {
                case 0:
                    value = data1;
                    break;
                case 1:
                    value = data2;
                    break;
                case 2:
                    value = data3;
                    break;
                case 3:
                    value = data4;
                    break;
            }

            if (item.type == ExDescription.TYPE_TIME) {
                int time = (int) value;
                builder.append(String.format("%d:%02d", time / 60, time % 60));
            } else {
                boolean isFloating = ((double) ((int) item.diff)) != item.diff;

                if (isFloating) {
                    builder.append(String.format("%.1f%s", value, item.label));
                } else {
                    builder.append(String.format("%d%s", (int) value, item.label));
                }
            }
            if (item.operator != null && item.operator.length() > 0) {
                builder.append(" " + item.operator + " ");
            } else {
                if (i != items.size() - 1)
                    builder.append(", ");
            }
        }
        return builder.toString();
    }

    public static class Item {
        public final String columnName;
        public int type;
        public double min;
        public double max;
        public double diff;
        public double defaultValue;
        public String label;
        public String operator;
        public String name;

        public Item(String columnName, int type, String name) {
            this.columnName = columnName;
            this.name = name;
            this.type = type;
        }
    }
}
