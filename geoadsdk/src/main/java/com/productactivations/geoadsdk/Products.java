package com.productactivations.geoadsdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Products {

    String productUrl, user_email;
    ProductsViewMode viewMode;

    public void autoLogin(String email, ProductsViewMode viewMode){

        this.user_email = email;
        this.viewMode = viewMode;

    }



    public void launchProductsView(Context context){

        switch(this.viewMode){
            case OUT_APP:
                launchProductsViewInOutBrowser(context);
                break;
        }
    }

    public String getAuthenticatedProductUrl(){
        String url = Config.products_url + Config.plugin_login_url + "?e="+user_email+"&auth="+this.getAuthCode();
        return url;
    }


    private String getAuthCode(){

       return "amcirujwpwqq1111349fkifklvmnvbfn2727j202838a";

    }

    public void launchProductsViewInOutBrowser(Context context){

        String url = Config.products_url + Config.plugin_login_url + "?e="+user_email+"&auth="+this.getAuthCode();
        EasyLogger.log("products url is " + url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }
}
