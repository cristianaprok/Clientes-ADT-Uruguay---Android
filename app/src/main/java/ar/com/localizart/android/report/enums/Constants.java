package ar.com.localizart.android.report.enums;

public interface Constants {

    // URLs:
    String ADD_URL = "https://www.adtfindu.com/gateway/alta_mobile.php?";

    String FINAL_URL = "https://www.adtfindu.com/mobile_app/index-app.php?";

    String STATUS_URL = "https://www.adtfindu.com/gateway/status.php?";

    /**
     * Main report URL (used to report data and for panic, it's the only one
     * used to retrieve parameters).
     */
    String MAIN_REPORT_URL = "https://www.adtfindu.com/gateway/bb50025.php";

    /**
     * Additional reporting URLs (to send panic only).
     */
    String[] ADDITIONAL_REPORT_URLS = { //
            // Si uso https para mappsme.com, me tira un error de que no esta
            // configurado el SSL, asi que usamos http por ahora.
//	"http://www.mappsme.com/gateway/bb50025.php" //
    };

    // States:
    boolean STATE_PANIC = true;
    boolean STATE_NORMAL = false;

    String ACTION_WEARABLE_PANIC_ALERT = "action_wearable_panin_alert";
}
