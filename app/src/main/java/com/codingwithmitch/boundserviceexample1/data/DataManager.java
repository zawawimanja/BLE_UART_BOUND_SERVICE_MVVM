package com.codingwithmitch.boundserviceexample1.data;

import com.codingwithmitch.boundserviceexample1.service.MyService;



/**
 * Created by Ali Esa Assadi on 26/03/2018.
 */

public class DataManager {

    private static DataManager sInstance;

    private DataManager() {
        // This class is not publicly instantiable
    }

    public static synchronized DataManager getInstance() {
        if (sInstance == null) {
            sInstance = new DataManager();
        }
        return sInstance;
    }


    public MyService getService() {
        return MyService.getInstance();
    }

}
