package com.github.maximaba.address.controller;

import com.github.maximaba.address.MainApp;
import com.github.maximaba.address.model.Person;
import com.github.maximaba.address.util.DateUtil;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;


public class PersonOverviewController implements Initializable {
    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, String> firstNameColumn;
    @FXML
    private TableColumn<Person, String> lastNameColumn;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label birthdayLabel;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private TextField searchField;

    // Ссылка на главное приложение.
    private MainApp mainApp;
    private ResourceBundle resourceBundle;
    private FilteredList<Person> filteredData;

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.resourceBundle = resources;

        // Инициализация таблицы адресатов с двумя столбцами.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        // Очистка дополнительной информации об адресате.
        showPersonDetails(null);

        // Слушаем изменения выбора, и при изменении отображаем
        // дополнительную информацию об адресате.
        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue));

        // Фильтр(Поиск)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(person -> {
            // Если текст фильтра(searchField) пуст, отображаем всех пользователей.
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            // Сравниваем имя и фамилию каждого человека с текстом фильтра(searchField).
            String lowerCaseFilter = newValue.toLowerCase();

            if (person.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                return true; // Совпадение по имени.
            } else if (person.getLastName().toLowerCase().contains(lowerCaseFilter)) {
                return true; // Совпадение по фамилии.
            }
            return false; // Нет совпадений.
        }));
    }

    /**
     * Вызывается главным приложением, которое даёт на себя ссылку.
     *
     * @param mainApp mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Добавление в таблицу данных из наблюдаемого списка
        personTable.setItems(mainApp.getPersonData());
    }

    /**
     * Заполняет все текстовые поля, отображая подробности об адресате.
     * Если указанный адресат = null, то все текстовые поля очищаются.
     *
     * @param person — адресат типа Person или null
     */
    private void showPersonDetails(Person person) {
        if (person != null) {
            // Заполняем метки информацией из объекта person.
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            streetLabel.setText(person.getStreet());
            postalCodeLabel.setText(Integer.toString(person.getPostalCode()));
            cityLabel.setText(person.getCity());
            birthdayLabel.setText(DateUtil.format(person.getBirthday()));
            phoneNumberLabel.setText(person.getPhoneNumber());
        } else {
            // Если Person = null, то убираем весь текст.
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");
            postalCodeLabel.setText("");
            cityLabel.setText("");
            birthdayLabel.setText("");
            phoneNumberLabel.setText("");
        }
    }

    /**
     * Вызывается, когда пользователь кликает по кнопке удаления.
     */
    @FXML
    private void handleDeletePerson() {
        int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            personTable.getItems().remove(selectedIndex);
            mainApp.setSaved();
        } else {
            // Ничего не выбрано.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle(resourceBundle.getString("key.error"));
            alert.setHeaderText(resourceBundle.getString("key.error.nps.header"));
            alert.setContentText(resourceBundle.getString("key.error.nps.context"));

            alert.showAndWait();
        }
    }

    /**
     * Вызывается, когда пользователь кликает по кнопке New...
     * Открывает диалоговое окно с дополнительной информацией нового адресата.
     */
    @FXML
    private void handleNewPerson() {
        Person tempPerson = new Person();
        boolean okClicked = mainApp.showPersonEditDialog(tempPerson);
        if (okClicked) {
            mainApp.getPersonData().add(tempPerson);
            mainApp.setSaved();
        }
    }

    /**
     * Вызывается, когда пользователь кликает по кнопка Edit...
     * Открывает диалоговое окно для изменения выбранного адресата.
     */
    @FXML
    private void handleEditPerson() {
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            boolean okClicked = mainApp.showPersonEditDialog(selectedPerson);
            if (okClicked) {
                showPersonDetails(selectedPerson);
                mainApp.setSaved();
            }

        } else {
            // Ничего не выбрано.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle(resourceBundle.getString("key.error"));
            alert.setHeaderText(resourceBundle.getString("key.error.nps.header"));
            alert.setContentText(resourceBundle.getString("key.error.nps.context"));

            alert.showAndWait();
        }
    }

    @FXML
    private void searchPerson() {
        // Оборачиваем ObservableList в FilteredList.
        filteredData = new FilteredList<>(mainApp.getPersonData(), p -> true);

        // Оборачиваем FilteredList в SortedList(FilteredList является immutable, поэтому он не может быть отсортирован).
        SortedList<Person> sortedData = new SortedList<>(filteredData);

        // Привязка компаратора SortedList к компоратору TableView.
        sortedData.comparatorProperty().bind(personTable.comparatorProperty());

        // Добавление отсортированные и отфилтрованные данные в TableView.
        personTable.setItems(sortedData);
    }
}