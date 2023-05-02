package ohm.softa.a07.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import ohm.softa.a07.api.OpenMensaAPI;
import ohm.softa.a07.model.Meal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {

	// use annotation to tie to component in XML
	@FXML
	private Button btnRefresh;
	@FXML
	private Button btnClose;
	@FXML
	private CheckBox chkVegetarian;

	@FXML
	private ListView<String> mealsList;
	private ObservableList<Meal> meals;

	private OpenMensaAPI api = null;


	class RefreshCallback implements Callback<List<Meal>> {
		@Override
		public void onResponse(Call<List<Meal>> call, Response<List<Meal>> response) {
			if (response.isSuccessful()) {
				assert response.body() != null;
				meals = response.body().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));
				ObservableList<String> list = meals.stream().map(Meal::toString).collect(Collectors.toCollection(FXCollections::observableArrayList));
				mealsList.setItems(list);
			} else {
				System.out.println("Request failed: " + response.body());
			}
		}

		@Override
		public void onFailure(Call<List<Meal>> call, Throwable t) {
			System.out.println("Request failed horribly:" + t.getMessage());
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Retrofit retrofit = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl("https://openmensa.org/api/v2/")
			.build();

		api = retrofit.create(OpenMensaAPI.class);


		// set the event handler (callback)
		btnRefresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// create a new (observable) list and tie it to the view
				refreshData();
			}
		});

		chkVegetarian.setOnAction(event -> {
			if (chkVegetarian.isSelected()) {
				ObservableList<String> list = meals.stream().filter(Meal::isVegetarian).map(Meal::toString).collect(Collectors.toCollection(FXCollections::observableArrayList));
				mealsList.setItems(list);
			} else {
				ObservableList<String> list = meals.stream().map(Meal::toString).collect(Collectors.toCollection(FXCollections::observableArrayList));
				mealsList.setItems(list);
			}
		});

		btnClose.setOnAction(event -> {
			Platform.exit();
		});
	}

	private void refreshData() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String today = dateFormat.format(new Date());
		api.getMeals(today).enqueue(new RefreshCallback());
	}
}
