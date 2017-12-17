package kulkarni.aditya.audioplayer;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by adicool on 16/12/17.
 */

public interface WebAPI {
    @GET("/studio")
    Call<ArrayList<Song>> getSongList();
}
