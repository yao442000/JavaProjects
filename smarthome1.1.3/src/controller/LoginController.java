package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tool.SystemTool;
import util.AppliactionUtil;
import util.AutoLoginUtil;
import util.HttpUtil;
import util.UiUtil;
import app.SmartHome;
import beans.User;

import communication.CommunicationUtil;

/**
 * ��¼������ <br/>
 * @author yinger
 * 
 */
public class LoginController implements Initializable {

	private double initX;
	private double initY;
	private Stage stage;// login stage''
	
	private boolean login = false;

	private User user;
	private AutoLoginUtil alu;
	private CommunicationUtil communicateUtil;
	private UiUtil uiUtil;

	//�ӷ��������ص�����
	private final int SERVER_ERROR = 4;  //�û������������
	private final int SERVER_USERNOEXIST = 2;  //�˻�������
	private final int SERVER_GATENOEXIST = 3;  //���ز�����
	private final int SERVER_LOCK = 7;  //Զ������
	
	
	@FXML
	private AnchorPane loginPane;// correspondent to fx:id
	@FXML
	private TextField tfName;
	@FXML
	private PasswordField tfPassword;
	@FXML
	private CheckBox cbRemeber;
	@FXML
	private CheckBox cbAutoLogin;
	@FXML
	private RadioButton rbGateway;
	@FXML
	private RadioButton rbServer;

	@FXML
	private AnchorPane loginWaitingPane;// loginWaitingPane show progress bar
	@FXML
	private AnchorPane loginViewPane;// login form

	@FXML
	private Label warningMessage;// show warning message
	@FXML
	private ImageView ivLogin;
	@FXML
	private ImageView ivCancle;

	@FXML
	private ImageView ivWinmin;
	@FXML
	private ImageView ivWinclose;
	@FXML
	private Button btnConfig; 
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initEvents();
		initValues();
	}

	/**
	 * ��ʼ������
	 */
	private void initValues() {
		stage = SmartHome.getAppStage();
		uiUtil = UiUtil.getInstance();
		alu = AutoLoginUtil.getInstance();
		communicateUtil = CommunicationUtil.getInstance();
		warningMessage.setText("");// notice
		// ����ϵͳ������Ϣ,����user��Ϣ
		try {
			SystemTool.getInstance().readSystemXml();
			user = alu.readObject();
		} catch (Exception e) {
			uiUtil.showErrorMessage(warningMessage, e.getMessage());// �ܹ���ȷ������
		}
		// ����user��Ϣ
//		try {
//			user = alu.readObject();
//		} catch (Exception e) {
//			uiUtil.showErrorMessage(warningMessage, e.getMessage());
//		}
		if (user == null) {// û���û���Ϣ�Ļ��ͽ����½����
			user = new User();
			gotoLoginView();
		} else {
			tfName.setText(user.getName());
			if (user.getLoginType() == CommunicationUtil.GATEWAY_LOGIN) {// ��������ģʽ--��
				rbGateway.setSelected(true);
			} else {
				rbServer.setSelected(true);
			}
			if (user.getLogin() == AutoLoginUtil.RemPwd) {// ���õ�½ģʽ
				cbRemeber.setSelected(true);
				tfPassword.setText(user.getPassword());
				gotoLoginView();
			} else if (user.getLogin() == AutoLoginUtil.AutoLogin) {
				cbRemeber.setSelected(true);
				cbAutoLogin.setSelected(true);
				tfPassword.setText(user.getPassword());
				gotoLoginWaiting();// �������ֱ�ӽ����½�ȴ��������Զ���½
				autoLogin();
			} else {// Ĭ����Ҫ�����½����
				gotoLoginView();
			}
		}
	}

	/**
	 * �¼�����
	 */
	private void initEvents() {
		// when mouse button is pressed, save the initial position of screen
		loginPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				initX = me.getScreenX() - stage.getX();
				initY = me.getScreenY() - stage.getY();
				stage.toFront();
			}
		});

		// when screen is dragged, translate it accordingly
		loginPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				stage.setX(me.getScreenX() - initX);
				stage.setY(me.getScreenY() - initY);
			}
		});
		//����������¼������û�������д���û������������԰�ס�س���ֱ�ӵ�¼
		loginPane.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent e){
				if(e.getCode() == KeyCode.ENTER){
					handleTryLogin();
				}
			}
		});
		ivLogin.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				handleTryLogin();
			}
		});
		
		ivCancle.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				cancleLogin();
			}
		});

		btnConfig.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				handleConfigServer();
			}
		});

		ivWinmin.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				stage.setIconified(true);
			}
		});

		ivWinclose.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				
				 //Platform.exit();
				System.exit(0);// ���ò���ǿ��Ա�֤�����������ص��̶߳�����
			}
		});
		
		cbAutoLogin.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				// TODO Auto-generated method stub
				if(newValue){
					cbRemeber.setSelected(true);
				}
			}

			
		});
	}

	/**
	 * ��¼����
	 */
	private void gotoLoginView() {
		loginWaitingPane.setVisible(false);
		loginViewPane.setVisible(true);
	}

	/**
	 * ��¼�ȴ�
	 */
	private void gotoLoginWaiting() {
		loginViewPane.setVisible(false);
		loginWaitingPane.setVisible(true);
	}

	/**
	 * ȡ����½
	 */
	private void cancleLogin() {
		login = false;
		communicateUtil.closeSocket();// �Ͽ�����
		gotoLoginView();
	}

	/**
	 * �Զ���¼
	 */
	private void autoLogin() {
		login = true;
		warningMessage.setText("");// ����Ϣ�ÿ�
		gotoLoginWaiting();// �л�����¼�ȴ�����
		new LoginThread(user.getLoginType(), user.getLogin(), user.getName(), user.getPassword()).start();// ������¼�߳�
	}

	/**
	 * �������ӵ�¼
	 */
	private void handleTryLogin() {
		String name = tfName.getText().trim();
		String password = tfPassword.getText().trim();
		if ("".equalsIgnoreCase(name) || "".equalsIgnoreCase(password)) {
			uiUtil.showErrorMessage(warningMessage, "�˺ź����붼����Ϊ�գ�");
			return;
		}
		login = true;
		warningMessage.setText("");// ����Ϣ�ÿ�
		int mode = cbAutoLogin.isSelected() ? AutoLoginUtil.AutoLogin : (cbRemeber.isSelected() ? AutoLoginUtil.RemPwd
				: AutoLoginUtil.NoLogin);
		int type = rbGateway.isSelected() ? CommunicationUtil.GATEWAY_LOGIN : CommunicationUtil.SERVER_LOGIN;// ��ǰѡ��ĵ�¼��ʽ
		gotoLoginWaiting();// �л�����¼�ȴ�����
		new LoginThread(type, mode, name, password).start();// ������¼�߳�
	}

	/**
	 * �򿪲������ô���
	 */
	private void handleConfigServer() {
		uiUtil.openDialog("Config.fxml");
	}

	/**
	 * ��¼�߳�
	 */
	class LoginThread extends Thread {
		private int type;
		private int mode;
		private String name;
		private String pwd;

		public LoginThread(int type, int mode, String name, String pwd) {
			this.type = type;
			this.mode = mode;
			this.name = name;
			this.pwd = pwd;
		}

		@Override
		public void run() {
			try {// ��ͨ�Ź��� ��������ص�����,Զ�̷���ʱ �Զ��ȷ��ʻ���ip����ʧ��������Զ��IP
				changeMessage("���ڽ�������......");
				int result = communicateUtil.buildCommunication(type, name, pwd);
				if(AppliactionUtil.DEBUG) System.out.println("��¼result��"+result);
//���������ж�û��Ҫ�ã���Ϊ��������ʱ������û�������Ϊ��ʱ���Ͱ�login��Ϊtrue�ˣ�loginΪfalse����ִ�е��������Ϊ��֤�ں�
//				if (!login) {
//					return;
//				}
				if (result == 1) {
					//���س������������
					if(communicateUtil.getLoginresult().toString().equals("���س������������")){
						if(AppliactionUtil.DEBUG) System.out.println("why");
						communicateUtil.closeSocket();// �Ͽ�����						
						backToLoginView(communicateUtil.getLoginresult().toString());
						communicateUtil.setLoginresult(new StringBuffer(""));
						return;
					}
												
					changeMessage("������֤�û���Ϣ......");
					if (communicateUtil.isLoginSuccess(name, pwd)) { // ��¼�ɹ�!
//						if (!login) {
//							return;
//						}
						user.setName(name);
						user.setPassword(pwd);
						user.setLoginType(type);
						user.setLogin(mode);
						// user.setFirstRun(false);
						SystemTool.CURRENT_USER = user;// ���õ�ǰ��¼���û�
//						if (!login) {
//							return;
//						}
						// ���ǲ��ܷ����߳���ִ�У���Ϊ���Ⱥ�ֻ�����������ļ��ſ��Լ��ؽ���
						// �����û���Ϣ
						try {
							changeMessage("���ڱ����û���Ϣ......");
							alu.writeObject(user);
							if (user.isAutodownable()) {// ���û��Ƿ��Զ�����
								changeMessage("���������豸�ļ�......");
								HttpUtil.getInstance().downloadFiles();// �����ļ� ����ļ�����û�з��ڵ������߳��У������������¼�߳���
							}
							changeMessage("���ڼ����豸״̬��Ϣ......");
							SystemTool.getInstance().initDeviceState();// �����豸״̬��Ϣ
						} catch (Exception e) {
							uiUtil.showErrorMessage(warningMessage, e.getMessage());
						}
						// ��һЩ�Ƚϸ��ӵ��������������У�
//						if (!login) {
//							return;
//						}
//						try {
//							if (user.isAutodownable()) {// ���û��Ƿ��Զ�����
//								changeMessage("���������豸�ļ�......");
//								HttpUtil.getInstance().downloadFiles();// �����ļ� ����ļ�����û�з��ڵ������߳��У������������¼�߳���
//							}
//							changeMessage("���ڼ����豸״̬��Ϣ......");
//							SystemTool.getInstance().initDeviceState();// �����豸״̬��Ϣ
//						} catch (Exception e) {
//							uiUtil.showErrorMessage(warningMessage, e.getMessage());
//						}
//						if (!login) {
//							return;
//						}
						Platform.runLater(new Runnable() {// ��ʼ����������
							public void run() {
								stage.hide();
								stage.close();// ��һ�仹�Ǽ��ϱȽϺã�Ϊ�˱�����ʾ��ʱ�򣬵�½��û����ʧ��������ͳ����˵����
								//stage.getOnCloseRequest();
								//stage.getOwner().hide();
								//stage.getOwner().setOnCloseRequest(null);
								SmartHome.getApplication().gotoMainPage();
							}
						});
					} else {// ��½�ɹ� communicateUtil.isLoginSuccess(name, pwd) -- ��½ʧ��
						if(AppliactionUtil.DEBUG) System.out.println("why");
						communicateUtil.closeSocket();// �Ͽ�����
						backToLoginView(communicateUtil.getLoginresult().toString());
					}
					
					
				} else if(result == SERVER_ERROR){
					communicateUtil.closeSocket();// �Ͽ�����
					backToLoginView("����ʧ�ܣ��û������������");
				}else if(result == SERVER_USERNOEXIST){
					communicateUtil.closeSocket();// �Ͽ�����
					backToLoginView("����ʧ�ܣ��˻������ڣ�");
				}else if(result == SERVER_GATENOEXIST){
					communicateUtil.closeSocket();// �Ͽ�����
					backToLoginView("����ʧ�ܣ����ز����ߣ�");
				}else if(result == SERVER_LOCK){
					communicateUtil.closeSocket();// �Ͽ�����
					backToLoginView("����ʧ�ܣ�Զ�̷�������������ϵ��Ӧ�̽�����");
				}else
				{// result ==1
					communicateUtil.closeSocket();// �Ͽ�����
					backToLoginView("����ʧ�ܣ�����ϸ���������ú�����״̬��");
				}
			} catch (Exception e) {
				communicateUtil.closeSocket();// �Ͽ�����
				backToLoginView("���Ӵ����쳣�������µ�¼��");
			}
		}
	}

	/**
	 * ��ʾ��Ϣ
	 */
	private void changeMessage(final String string) {
		Platform.runLater(new Runnable() {// go to javafx application thread
			public void run() {
				warningMessage.setText(string);
				warningMessage.setTextFill(Color.BLUE);
			}
		});
	}

	/**
	 * ��ʾ��Ϣ���ص���½����
	 */
	private void backToLoginView(final String string) {
		Platform.runLater(new Runnable() {// go to javafx application thread
			public void run() {
				warningMessage.setText(string);
				warningMessage.setTextFill(Color.RED);
				gotoLoginView();
			}
		});
	}
}