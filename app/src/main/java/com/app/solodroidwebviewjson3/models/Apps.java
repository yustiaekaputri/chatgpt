package com.app.solodroidwebviewjson3.models;

import java.io.Serializable;

public class Apps implements Serializable {

    public boolean status;
    public String redirect_url = "";
    public String privacy_policy_url = "";
    public String user_agent = "";
    public boolean navigation_drawer = true;
    public boolean navigation_icon_solid_color = true;
    public boolean geolocation = true;
    public boolean webview_cache = true;
    public boolean open_link_in_external_browser = true;
    public boolean enable_rtl_mode = true;
    public boolean enable_exit_dialog = true;
    public boolean enable_vpn_access = false;
    public boolean enable_dark_mode_as_default_theme = false;

}
