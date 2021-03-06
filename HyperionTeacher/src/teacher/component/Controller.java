package teacher.component;

import teacher.network.client.DedicatedServer;
import teacher.network.server.LocalServer;
import teacher.entity.Class;
import teacher.entity.Student;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    @FXML
    private RadioButton localModeRadioBtn;
    @FXML
    private RadioButton dedicatedModeRadioBtn;
    @FXML
    private TextField IPTxtField;
    @FXML
    private ComboBox<String> classComboBox;
    @FXML
    private Button connectBtn;
    @FXML
    private ToggleButton toggleCheckInBtn;
    @FXML
    private Slider timesSlider;
    @FXML
    private TableView<Student> tableView;
    @FXML
    private TableColumn<Student, String> stuIdCol;
    @FXML
    private TableColumn<Student, String> stuMacCol;
    @FXML
    private TableColumn<Student, String> col1;
    @FXML
    private TableColumn<Student, String> col2;
    @FXML
    private TableColumn<Student, String> col3;
    @FXML
    private TableColumn<Student, String> col4;
    @FXML
    private TableColumn<Student, String> col5;
    @FXML
    private TableColumn<Student, String> col6;
    @FXML
    private TableColumn<Student, String> col7;
    @FXML
    private TableColumn<Student, String> col8;
    @FXML
    private TableColumn<Student, String> col9;

    private TableColumn[] tableColumns;

    private CheckInManager checkInManager;

    private LocalServer localServer;
    private DedicatedServer dedicatedServer;

    @FXML
    // 初始化
    protected void initialize() {
        checkInManager = CheckInManager.getInstance();
        localServer = LocalServer.getInstance();
        dedicatedServer = DedicatedServer.getInstance();

        localServer.setTableView(tableView);
        dedicatedServer.setTableView(tableView);

        tableColumns = new TableColumn[]{col1, col2, col3, col4, col5, col6, col7, col8, col9};

        stuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        stuMacCol.setCellValueFactory(new PropertyValueFactory<>("mac"));
        bindIsChecked(5);

        localServer.launch();
        fillComboBox();
    }

    @FXML
    // 切换局域网/公网模式
    protected void switchMode() {
        if (dedicatedModeRadioBtn.isSelected()) {
            IPTxtField.setDisable(false);
            connectBtn.setDisable(false);
            toggleCheckInBtn.setDisable(true);
            classComboBox.setDisable(true);
            timesSlider.setDisable(true);

            localServer.close();
            classComboBox.getItems().clear();
            tableView.getItems().clear();
        } else {
            IPTxtField.setDisable(true);
            connectBtn.setDisable(true);
            toggleCheckInBtn.setDisable(false);
            classComboBox.setDisable(false);
            timesSlider.setDisable(false);

            classComboBox.getItems().clear();
            tableView.getItems().clear();

            if (dedicatedServer.getIsConnected()) {
                dedicatedServer.close();
            }

            localServer.launch();
            fillComboBox();
        }
    }

    @FXML
    // 设置当前签到次数
    private void setCurrentCheckInCount() {
        int curr = (int) timesSlider.getValue();

        if (curr < 6) {
            bindIsChecked(5);
            for (int i = 0; i < 9; i++)
                tableColumns[i].setText(Integer.toString(i + 1));
        } else if (curr > 15) {
            bindIsChecked(16);
            for (int i = 0; i < 9; i++)
                tableColumns[i].setText(Integer.toString(i + 12));
        } else {
            bindIsChecked(curr);
            for (int i = 0; i < 9; i++)
                tableColumns[i].setText(Integer.toString(curr - 4 + i));
        }
    }

    @FXML
    // 开启或关闭签到
    protected void StartCheckIn() {
        if (toggleCheckInBtn.isSelected()) {
            if (classComboBox.getValue() == null || classComboBox.getValue().equals("")) {
                showSimpleAlert(Alert.AlertType.WARNING, "Waring", "You need to choose a class to start check-in");
                toggleCheckInBtn.setSelected(false);
                return;
            }

            toggleCheckInBtn.setText("Stop Check-In");
            timesSlider.setDisable(true);
            classComboBox.setDisable(true);
            dedicatedModeRadioBtn.setDisable(true);
            localModeRadioBtn.setDisable(true);

            checkInManager.setCurrentClass(classComboBox.getValue());
            if (checkInManager.getCurrentClass() == null) {
                checkInManager.getClasses().put(classComboBox.getValue(), new Class(classComboBox.getValue(), (int) timesSlider.getValue()));
                fillComboBox();
            } else
                checkInManager.getCurrentClass().setCheckInCount((int) timesSlider.getValue());

            if (dedicatedModeRadioBtn.isSelected() && dedicatedServer.getIsConnected()) {
                TextInputDialog dialog = new TextInputDialog(getRouterMac());
                dialog.setTitle("Set target router MAC address");
                dialog.setHeaderText("");
                dialog.setContentText("Please set the target router MAC address");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    dedicatedServer.startCheckIn(result.get(), classComboBox.getValue(), (int) timesSlider.getValue());
                    connectBtn.setDisable(true);
                } else {
                    toggleCheckInBtn.setSelected(false);
                    toggleCheckInBtn.setText("Start Check-In");
                    timesSlider.setDisable(false);
                    classComboBox.setDisable(false);
                    dedicatedModeRadioBtn.setDisable(false);
                    localModeRadioBtn.setDisable(false);
                    connectBtn.setDisable(false);

                    checkInManager.setCurrentClass(null);
                }
            }
        } else {
            toggleCheckInBtn.setText("Start Check-In");
            timesSlider.setDisable(false);
            classComboBox.setDisable(false);
            dedicatedModeRadioBtn.setDisable(false);
            localModeRadioBtn.setDisable(false);

            checkInManager.setCurrentClass(null);

            if (dedicatedModeRadioBtn.isSelected()) {
                dedicatedServer.stopCheckIn();
                connectBtn.setDisable(false);
            }
        }
    }

    @FXML
    // 公网模式连接到服务器
    protected void connectToDedicatedServer() {
        if (IPTxtField.getText() == null || IPTxtField.getText().equals("")) {
            showSimpleAlert(Alert.AlertType.ERROR, "Error", "Please input the Dedicated server IP");
            return;
        }

        if (!DedicatedServer.getInstance().getIsConnected()) {
            Thread thread = new Thread(() -> {
                try {
                    Platform.runLater(() -> {
                        localModeRadioBtn.setDisable(true);
                        dedicatedModeRadioBtn.setDisable(true);
                        connectBtn.setText("Connecting...");
                        connectBtn.setDisable(true);
                    });

                    if (dedicatedServer.connectToServer(IPTxtField.getText())) {
                        fillComboBox();

                        Platform.runLater(() -> {
                            localModeRadioBtn.setDisable(false);
                            dedicatedModeRadioBtn.setDisable(false);
                            toggleCheckInBtn.setDisable(false);
                            classComboBox.setDisable(false);
                            timesSlider.setDisable(false);
                            connectBtn.setText("Disconnect");
                            connectBtn.setDisable(false);
                            IPTxtField.setDisable(true);
                        });
                    } else {
                        Platform.runLater(() -> {
                            showSimpleAlert(Alert.AlertType.ERROR, "Error", "Dedicated server connect fail, there is already a teacher connecting");
                            connectBtn.setText("Connect");
                            connectBtn.setDisable(false);
                            localModeRadioBtn.setDisable(false);
                            dedicatedModeRadioBtn.setDisable(false);
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showSimpleAlert(Alert.AlertType.ERROR, "Error", "Cannot connect to the server, please check your network or the Dedicated server IP");
                        connectBtn.setText("Connect");
                        connectBtn.setDisable(false);
                        localModeRadioBtn.setDisable(false);
                        dedicatedModeRadioBtn.setDisable(false);
                    });
                }
            });
            thread.start();
        } else {
            DedicatedServer.getInstance().close();
            connectBtn.setText("Connect");
            connectBtn.setDisable(false);
            localModeRadioBtn.setDisable(false);
            dedicatedModeRadioBtn.setDisable(false);
            IPTxtField.setDisable(false);
            classComboBox.setDisable(true);
            timesSlider.setDisable(true);
            classComboBox.getItems().clear();
            tableView.getItems().clear();
        }
    }

    @FXML
    // 填充签到表
    public void fillTableView() {
        if (checkInManager.getClasses() == null || classComboBox.getValue() == null || checkInManager.getClasses().get(classComboBox.getValue()) == null)
            tableView.getItems().clear();
        else {
            ObservableList<Student> studentObservableList = FXCollections.observableArrayList(checkInManager.getClasses().get(classComboBox.getValue()).getStudentsInClass().values());
            tableView.setItems(studentObservableList);
        }
    }

    // 绑定签到次数列
    private void bindIsChecked(int count) {
        col1.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 4)));
        col2.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 3)));
        col3.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 2)));
        col4.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count - 1)));
        col5.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count)));
        col6.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 1)));
        col7.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 2)));
        col8.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 3)));
        col9.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsChecked(count + 4)));
        tableView.refresh();
    }

    // 填充班级下拉菜单
    private void fillComboBox() {
        classComboBox.getItems().clear();

        for (String classId : checkInManager.getClasses().keySet())
            if (!classComboBox.getItems().contains(classId))
                classComboBox.getItems().add(classId);
    }

    // 得到第一跳路由器（默认网关）的MAC地址
    private String getRouterMac() {
        try {
            Pattern pattern;
            Matcher matcher;
            String keyword;
            int index;

            // 得到默认网关的IP地址
            String gateway = callCmd("ipconfig");
            keyword = (gateway.contains("Default Gateway") ? "Default Gateway" : "默认网关") + "(.\\s)+:\\s";
            pattern = Pattern.compile(keyword);
            matcher = pattern.matcher(gateway);
            matcher.find();
            index = matcher.end();
            gateway = gateway.substring(index, gateway.indexOf("\n", index)).trim();

            // 得到默认网关的MAC地址
            String mac = callCmd("arp -a");
            keyword = gateway + "\\s+";
            pattern = Pattern.compile(keyword);
            matcher = pattern.matcher(mac);
            matcher.find();
            index = matcher.end();
            return mac.substring(index, index + 17).toUpperCase().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 调用CMD
    private String callCmd(String cmd) {
        StringBuilder result = new StringBuilder();
        String line;
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream(), "GBK");
            BufferedReader br = new BufferedReader(is);
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    // 弹出对话框
    private void showSimpleAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
