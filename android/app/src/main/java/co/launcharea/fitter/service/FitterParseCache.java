package co.launcharea.fitter.service;

import android.content.Context;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.launcharea.fitter.model.ExDescription;

/**
 * Created by jack on 15. 8. 20.
 */
public class FitterParseCache {
    static final List<ExDescription> listDescription = new LinkedList<>();
    static final Map<String, ExDescription> mapDescription = new HashMap<>();

    public static void init(Context context) {
        try {
            loadDescriptionMap();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void loadDescriptionMap() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Description");
        query.orderByAscending("descriptionId");
        List<ParseObject> list = query.find();
        for (ParseObject each : list) {
            each.fetchIfNeeded();
            ExDescription desc = new ExDescription(each.getObjectId(), each.getInt("version"));
            try {
                desc.loadItemFromJson("data1", each.getString("data1"));
                desc.loadItemFromJson("data2", each.getString("data2"));
                desc.loadItemFromJson("data3", each.getString("data3"));
                desc.loadItemFromJson("data4", each.getString("data4"));
                listDescription.add(desc);
                mapDescription.put(desc.getObjectId(), desc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ExDescription> listExDescriptions() {
        return listDescription;
    }

    public static ExDescription getExDescription(String objectId) {
        ExDescription obj = mapDescription.get(objectId);
        if (obj == null) {
            try {
                ParseObject po = ParseObject.createWithoutData("Description", objectId);
                po.fetchIfNeeded();
                ParseObject each = po;
                ExDescription desc = new ExDescription(each.getObjectId(), each.getInt("version"));
                try {
                    desc.loadItemFromJson("data1", each.getString("data1"));
                    desc.loadItemFromJson("data2", each.getString("data2"));
                    desc.loadItemFromJson("data3", each.getString("data3"));
                    desc.loadItemFromJson("data4", each.getString("data4"));
                    listDescription.add(desc);
                    mapDescription.put(desc.getObjectId(), desc);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return obj;
    }
}
