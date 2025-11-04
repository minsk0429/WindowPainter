import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame implements ActionListener, ChangeListener {
	private final String MENU_FILE_NEW = "새로만들기(N)";
	private final String MENU_FILE_CLOSE = "끝내기(E)";
	private final String MENU_FILE_OPEN = "열기(O)";
	private final String MENU_FILE_SAVE = "저장(S)";
	private final String[] TOOL_NAMES = {"점", "선", "원", "네모", "지우개", "텍스트", "선택", "삭제", "채우기"};
    
    private final String MENU_FILE_SAVE_IMAGE = "이미지 파일로 저장...";
    private final String MENU_FILE_OPEN_IMAGE = "이미지 파일 열기...";
    private final String MENU_VIEW_DEBUG = "디버그 정보 보기";
    
    private final String MENU_VIEW_FLIP_H = "좌우 대칭";
    private final String MENU_VIEW_FLIP_V = "상하 대칭";
    private final String MENU_VIEW_ROTATE_C = "90도 시계 방향 회전";
    private final String MENU_VIEW_ROTATE_CC = "90도 반시계 방향 회전";
    
    private final String BUTTON_FILL_SELECTION = "선택 영역 채우기";
	
	private final String BUTTON_LINE_COLOR = "선 색상";
	private final String BUTTON_FILL_COLOR = "채우기 색상";
	private final String BUTTON_COLOR_PICKER = "다른 색상...";
	
	private final int MIN_STROKE = 1;
	private final int MAX_STROKE = 10;
	private JSpinner strokeSpinner; 
	private JCheckBox fillCheckBox;
	
	private final int MIN_ERASER = 5;
	private final int MAX_ERASER = 30;
	private JSpinner eraserSpinner;
	
	private final int MIN_FONT_SIZE = 8;
	private final int MAX_FONT_SIZE = 48;
	private JSpinner fontSizeSpinner;
	
	private JLabel statusBar = null;
	private JButton []toolboxButtons;
	private JButton lineColorButton;
	private JButton fillColorButton;
	private JButton currentSelectedColorButton;
	private JButton []colorButtons;
	private Color []colors;
	private Screen screen;
    
    private float zoomLevel = 1.0f;
    private final float ZOOM_STEP = 0.5f;
    private final float MAX_ZOOM = 4.0f;
    private final float MIN_ZOOM = 0.5f;
    private JCheckBoxMenuItem debugMenuItem;
	
	public MainFrame() {
		screen = new Screen(); 
		
		Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
        screen.setBorder(border);
        
		add(screen);
		this.setJMenuBar(createMenuBar());
		this.statusBar = createStatusBar();
		this.add(statusBar, BorderLayout.SOUTH);
		this.add(createToolBar(), BorderLayout.NORTH);
		this.setSize(800, 600);
		this.setTitle("Windows Painter");
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JPanel groupPanelDrawBox = new JPanel();
		groupPanelDrawBox.setLayout(new GridLayout(5, 2));
		
		toolboxButtons = new JButton[9];
		
		JButton pointButton = new JButton(TOOL_NAMES[0]);
		pointButton.setPreferredSize(new Dimension(60, 30));
		pointButton.addActionListener(this);
		groupPanelDrawBox.add(pointButton);
		toolboxButtons[0] = pointButton;
		
		JButton lineButton = new JButton(TOOL_NAMES[1]);
		lineButton.setPreferredSize(new Dimension(60, 30));
		lineButton.addActionListener(this);
		groupPanelDrawBox.add(lineButton);
		toolboxButtons[1] = lineButton;

		JButton circleButton = new JButton(TOOL_NAMES[2]);
		circleButton.setPreferredSize(new Dimension(60, 30));
		circleButton.addActionListener(this);
		groupPanelDrawBox.add(circleButton);
		toolboxButtons[2] = circleButton;
		
		JButton rectButton = new JButton(TOOL_NAMES[3]);
		rectButton.setPreferredSize(new Dimension(60, 30));
		rectButton.addActionListener(this);
		groupPanelDrawBox.add(rectButton);
		toolboxButtons[3] = rectButton;
		
		JButton eraserButton = new JButton(TOOL_NAMES[4]);
		eraserButton.setPreferredSize(new Dimension(60, 30));
		eraserButton.addActionListener(this);
		groupPanelDrawBox.add(eraserButton);
		toolboxButtons[4] = eraserButton;
		
		JButton textButton = new JButton(TOOL_NAMES[5]);
		textButton.setPreferredSize(new Dimension(60, 30));
		textButton.addActionListener(this);
		groupPanelDrawBox.add(textButton);
		toolboxButtons[5] = textButton;
		
		JButton selectButton = new JButton(TOOL_NAMES[6]);
		selectButton.setPreferredSize(new Dimension(60, 30));
		selectButton.addActionListener(this);
		groupPanelDrawBox.add(selectButton);
		toolboxButtons[6] = selectButton;
		
		JButton deleteButton = new JButton(TOOL_NAMES[7]);
		deleteButton.setPreferredSize(new Dimension(60, 30));
		deleteButton.addActionListener(this);
		groupPanelDrawBox.add(deleteButton);
		toolboxButtons[7] = deleteButton;
		
		JButton fillBucketButton = new JButton(TOOL_NAMES[8]);
		fillBucketButton.setPreferredSize(new Dimension(60, 30));
		fillBucketButton.addActionListener(this);
		groupPanelDrawBox.add(fillBucketButton);
		toolboxButtons[8] = fillBucketButton;
		
		JPanel groupPanelEraser = new JPanel();
		groupPanelEraser.setBorder(BorderFactory.createTitledBorder("지우개 크기"));
		
		SpinnerNumberModel eraserModel = new SpinnerNumberModel(10, MIN_ERASER, MAX_ERASER, 5);
		eraserSpinner = new JSpinner(eraserModel);
		eraserSpinner.setPreferredSize(new Dimension(60, 30));
		eraserSpinner.addChangeListener(this); 
		groupPanelEraser.add(eraserSpinner);
		
		JPanel groupPanelFont = new JPanel();
		groupPanelFont.setBorder(BorderFactory.createTitledBorder("폰트 크기"));
		
		SpinnerNumberModel fontModel = new SpinnerNumberModel(16, MIN_FONT_SIZE, MAX_FONT_SIZE, 2);
		fontSizeSpinner = new JSpinner(fontModel);
		fontSizeSpinner.setPreferredSize(new Dimension(60, 30));
		fontSizeSpinner.addChangeListener(this); 
		groupPanelFont.add(fontSizeSpinner);
		
		JPanel groupPanelFillControl = new JPanel();
		groupPanelFillControl.setLayout(new GridLayout(2, 1));
		groupPanelFillControl.setBorder(BorderFactory.createTitledBorder("채우기"));
		
		fillCheckBox = new JCheckBox("도형 채우기 사용");
		fillCheckBox.setSelected(false);
		fillCheckBox.setPreferredSize(new Dimension(110, 20));
		fillCheckBox.addActionListener(this); 
		groupPanelFillControl.add(fillCheckBox);

		JButton fillSelectionButton = new JButton(BUTTON_FILL_SELECTION);
		fillSelectionButton.setPreferredSize(new Dimension(110, 20));
		fillSelectionButton.addActionListener(this);
		groupPanelFillControl.add(fillSelectionButton);
		
		JPanel groupPanelStroke = new JPanel();
		groupPanelStroke.setBorder(BorderFactory.createTitledBorder("선 두께"));
		
		SpinnerNumberModel model = new SpinnerNumberModel(1, MIN_STROKE, MAX_STROKE, 1);
		strokeSpinner = new JSpinner(model);
		strokeSpinner.setPreferredSize(new Dimension(60, 30));
		strokeSpinner.addChangeListener(this); 
		groupPanelStroke.add(strokeSpinner);
		
		JPanel groupPanelColorControl = new JPanel();
		groupPanelColorControl.setLayout(new GridLayout(3, 1));
		
		lineColorButton = new JButton(BUTTON_LINE_COLOR);
		lineColorButton.setBackground(Color.BLACK);
		lineColorButton.setPreferredSize(new Dimension(100, 20));
		lineColorButton.addActionListener(this);
		groupPanelColorControl.add(lineColorButton);
		currentSelectedColorButton = lineColorButton;
		
		fillColorButton = new JButton(BUTTON_FILL_COLOR);
		fillColorButton.setBackground(Color.WHITE);
		fillColorButton.setPreferredSize(new Dimension(100, 20));
		fillColorButton.addActionListener(this);
		groupPanelColorControl.add(fillColorButton);

		JButton colorPickerButton = new JButton(BUTTON_COLOR_PICKER);
		colorPickerButton.setPreferredSize(new Dimension(100, 20));
		colorPickerButton.addActionListener(this);
		groupPanelColorControl.add(colorPickerButton);
		
		JPanel groupPanelColorPalette = new JPanel();
		groupPanelColorPalette.setLayout(new GridLayout(3, 5));
		colorButtons = new JButton[15];
		
		colors = new Color[15];
		colors[0] = new Color(255, 255, 255);
		colors[1] = new Color(255, 0, 0);
		colors[2] = new Color(0, 255, 0);
		colors[3] = new Color(0, 0, 255);
		colors[4] = new Color(255, 255, 0);
		colors[5] = new Color(0, 255, 255);
		colors[6] = new Color(255, 0, 255);
		colors[7] = new Color(192, 192, 192);
		colors[8] = new Color(128, 128, 128);
		colors[9] = new Color(128, 0, 0);
		colors[10] = new Color(128, 128, 0);
		colors[11] = new Color(0, 128, 0);
		colors[12] = new Color(128, 0, 128);
		colors[13] = new Color(0, 128, 128);
		colors[14] = new Color(0, 0, 128);
		
		for(int i=0; i < colorButtons.length; i++) {
			colorButtons[i] = new JButton();
			JButton colorButton = colorButtons[i];
			colorButton.setBackground(colors[i]);
			colorButton.setPreferredSize(new Dimension(20, 20));
			colorButton.addActionListener(this);
			groupPanelColorPalette.add(colorButton);
		}
		
		toolbar.add(groupPanelDrawBox);
		toolbar.add(groupPanelEraser);
		toolbar.add(groupPanelFont);
		toolbar.add(groupPanelFillControl);
		toolbar.add(groupPanelStroke); 
		toolbar.add(groupPanelColorControl);
		toolbar.add(groupPanelColorPalette);
		return toolbar;
	}
	
	private JLabel createStatusBar() {
		JLabel statusBar = new JLabel("Ready");
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		return statusBar;
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		ImageIcon iconNew = new ImageIcon("resource/new.png");
		ImageIcon iconClose = new ImageIcon("resource/close.png");
		
		JMenu fileMenu = new JMenu("파일(F)");
		menuBar.add(fileMenu);
		
		JMenuItem newMenuItem = new JMenuItem(MENU_FILE_NEW, iconNew);
		newMenuItem.addActionListener(this);
		newMenuItem.setToolTipText("파일을 새로 만듭니다.");
		newMenuItem.setMnemonic(KeyEvent.VK_N);
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(newMenuItem);
		fileMenu.addSeparator();
        
        JMenuItem saveImageMenuItem = new JMenuItem(MENU_FILE_SAVE_IMAGE);
        saveImageMenuItem.addActionListener(this);
        fileMenu.add(saveImageMenuItem);
        
        JMenuItem openImageMenuItem = new JMenuItem(MENU_FILE_OPEN_IMAGE);
        openImageMenuItem.addActionListener(this);
        fileMenu.add(openImageMenuItem);
        fileMenu.addSeparator();
		
		JMenuItem saveMenuItem = new JMenuItem(MENU_FILE_SAVE);
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
		saveMenuItem.addActionListener(this);
		JMenuItem openMenuItem = new JMenuItem(MENU_FILE_OPEN);
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.addActionListener(this);
		JMenuItem saveasMenuItem = new JMenuItem("다른 이름으로 저장");
		fileMenu.add(saveMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(saveasMenuItem);
		fileMenu.addSeparator();
		
		JMenuItem closeMenuItem = new JMenuItem(MENU_FILE_CLOSE, iconClose);
		closeMenuItem.addActionListener(this);
		closeMenuItem.setToolTipText("프로그램을 종료 합니다.");
		closeMenuItem.setMnemonic(KeyEvent.VK_E);
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(closeMenuItem);
		
		JMenu viewMenu = new JMenu("보기(V)");
		
		JMenuItem zoomInMenuItem = new JMenuItem("확대 (Z)");
		zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		zoomInMenuItem.addActionListener(this);
		
		JMenuItem zoomOutMenuItem = new JMenuItem("축소 (X)");
		zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		zoomOutMenuItem.addActionListener(this);
		
		viewMenu.add(zoomInMenuItem);
		viewMenu.add(zoomOutMenuItem);
		viewMenu.addSeparator();

		JMenuItem flipHMenuItem = new JMenuItem(MENU_VIEW_FLIP_H);
		flipHMenuItem.addActionListener(this);
		viewMenu.add(flipHMenuItem);
		
		JMenuItem flipVMenuItem = new JMenuItem(MENU_VIEW_FLIP_V);
		flipVMenuItem.addActionListener(this);
		viewMenu.add(flipVMenuItem);
		
		JMenuItem rotateCMenuItem = new JMenuItem(MENU_VIEW_ROTATE_C);
		rotateCMenuItem.addActionListener(this);
		viewMenu.add(rotateCMenuItem);
		
		JMenuItem rotateCCMenuItem = new JMenuItem(MENU_VIEW_ROTATE_CC);
		rotateCCMenuItem.addActionListener(this);
		viewMenu.add(rotateCCMenuItem);
		
		viewMenu.addSeparator();

		debugMenuItem = new JCheckBoxMenuItem(MENU_VIEW_DEBUG);
		debugMenuItem.setSelected(false);
		debugMenuItem.addActionListener(this);
		viewMenu.add(debugMenuItem);
		viewMenu.addSeparator();
		
		JCheckBoxMenuItem showStatusMenuItem = new JCheckBoxMenuItem("상태바 보기(S)");
		showStatusMenuItem.setSelected(true);
		showStatusMenuItem.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					statusBar.setVisible(true);
				}
				else {
					statusBar.setVisible(false);
				}
			}
		});
		
		JMenu toolMenu = new JMenu("도구");

		menuBar.add(toolMenu);

		JMenuItem pointMenuItem = new JMenuItem("점(P)");
		pointMenuItem.addActionListener(this); 

		toolMenu.add(pointMenuItem);

		toolMenu.addSeparator();
		
		JMenuItem lineMenuItem = new JMenuItem("선(L)");
		lineMenuItem.addActionListener(this); 

		toolMenu.add(lineMenuItem);
		
		toolMenu.addSeparator();

		JMenuItem circleMenuItem = new JMenuItem("원(C)");
		circleMenuItem.addActionListener(this); 

		toolMenu.add(circleMenuItem);
		
		toolMenu.addSeparator();

		JMenuItem boxMenuItem = new JMenuItem("네모(B)");
		boxMenuItem.addActionListener(this); 

		toolMenu.add(boxMenuItem);

		menuBar.add(viewMenu);
		
		viewMenu.add(showStatusMenuItem);

		menuBar.add(toolMenu);

		return menuBar;

	}
    
    private void setZoom(float newZoomLevel) {
        if (newZoomLevel < MIN_ZOOM) {
            newZoomLevel = MIN_ZOOM;
        } else if (newZoomLevel > MAX_ZOOM) {
            newZoomLevel = MAX_ZOOM;
        }
        
        if (newZoomLevel != zoomLevel) {
            zoomLevel = newZoomLevel;
            screen.setZoomLevel(zoomLevel);
            statusBar.setText("확대/축소: " + (int)(zoomLevel * 100) + "%");
        }
    }
    
    private void updateDebugMode() {
        boolean isDebug = debugMenuItem.isSelected();
        screen.setDebugMode(isDebug);
        if (isDebug) {
            statusBar.setText("디버그 모드 활성화됨.");
        } else {
            statusBar.setText("디버그 모드 비활성화됨.");
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (cmd.equals(MENU_VIEW_DEBUG)) {
			updateDebugMode();
			return;
		}
        
        if (cmd.equals(MENU_VIEW_FLIP_H)) {
            screen.applyFlipHorizontal();
            statusBar.setText("이미지를 좌우 대칭했습니다.");
            return;
        }
        else if (cmd.equals(MENU_VIEW_FLIP_V)) {
            screen.applyFlipVertical();
            statusBar.setText("이미지를 상하 대칭했습니다.");
            return;
        }
        else if (cmd.equals(MENU_VIEW_ROTATE_C)) {
            screen.applyRotate90Degrees(true);
            statusBar.setText("이미지를 90도 시계 방향 회전했습니다.");
            return;
        }
        else if (cmd.equals(MENU_VIEW_ROTATE_CC)) {
            screen.applyRotate90Degrees(false);
            statusBar.setText("이미지를 90도 반시계 방향 회전했습니다.");
            return;
            
        }
		
		if(cmd.equals(MENU_FILE_NEW)) {
			screen.clearAll(); 
            statusBar.setText("새로운 그림을 시작합니다.");
		}
		else if(cmd.equals(MENU_FILE_SAVE)) {
			FileDialog fd = new FileDialog(this,"파일 저장", FileDialog.SAVE);
			fd.setVisible(true);
			if(fd.getFile() != null)
			{
				screen.save(fd.getDirectory()+fd.getFile());
			}
		}
		else if(cmd.equals(MENU_FILE_OPEN)) {
			FileDialog fd = new FileDialog(this,"파일 열기", FileDialog.LOAD);
			fd.setFile("*.sav");
			fd.setVisible(true);
			if(fd.getFile() != null)
			{
				screen.open(fd.getDirectory()+fd.getFile());
				screen.repaint();
			}
		}
		else if(cmd.equals(MENU_FILE_CLOSE)) {
			this.dispose();
			System.exit(0);
		}
        
        if (cmd.equals(MENU_FILE_SAVE_IMAGE)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Image (*.jpg)", "jpg", "jpeg"));
            
            int dialogResult = fileChooser.showSaveDialog(this);
            
            if (dialogResult == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                String path = file.getAbsolutePath();
                FileNameExtensionFilter filter = (FileNameExtensionFilter) fileChooser.getFileFilter();
                String ext = filter.getExtensions()[0];
                
                if (!path.toLowerCase().endsWith("." + ext)) {
                    file = new File(path + "." + ext);
                }
                
                boolean saveSuccess = screen.saveImage(file, ext);
                
                if (saveSuccess) {
                    statusBar.setText("이미지 파일 저장 완료: " + file.getName());
                } else {
                    statusBar.setText("이미지 파일 저장 실패!");
                }
            }
            return;
        } 
        else if (cmd.equals(MENU_FILE_OPEN_IMAGE)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));
            
            int dialogResult = fileChooser.showOpenDialog(this);
            
            if (dialogResult == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (screen.loadImage(file)) {
                    statusBar.setText("이미지 파일 열기 완료: " + file.getName());
                    screen.repaint();
                } else {
                    statusBar.setText("이미지 파일 열기 실패!");
                }
            }
            return;
        }
        
        if (cmd.equals("확대 (Z)")) {
			setZoom(zoomLevel + ZOOM_STEP);
			return;
		} else if (cmd.equals("축소 (X)")) {
			setZoom(zoomLevel - ZOOM_STEP);
			return;
		}
		
		if (e.getSource() == fillCheckBox) {
			boolean isFilled = fillCheckBox.isSelected();
			screen.setCurrentFill(isFilled);
			statusBar.setText("도형 채우기 상태: " + (isFilled ? "사용" : "미사용"));
			return;
		}
        
        if (cmd.equals(BUTTON_FILL_SELECTION)) {
            screen.fillSelectedArea(fillColorButton.getBackground());
            statusBar.setText("선택 영역을 채웠습니다.");
            return;
        }
		
		if (cmd.equals(BUTTON_LINE_COLOR)) {
			currentSelectedColorButton = lineColorButton;
			statusBar.setText("선 색상 선택 모드입니다.");
			return;
		}
		else if (cmd.equals(BUTTON_FILL_COLOR)) {
			currentSelectedColorButton = fillColorButton;
			statusBar.setText("채우기 색상 선택 모드입니다.");
			return;
		}
		else if (cmd.equals(BUTTON_COLOR_PICKER)) {
			Color initialColor = currentSelectedColorButton.getBackground();
			Color newColor = JColorChooser.showDialog(this, "색상 선택", initialColor);
			
			if (newColor != null) {
				currentSelectedColorButton.setBackground(newColor);
				
				if (currentSelectedColorButton == lineColorButton) {
					screen.setCurrentColor(newColor);
					statusBar.setText("선 색상 선택됨 (R:" + newColor.getRed() + ", G:" + newColor.getGreen() + ", B:" + newColor.getBlue() + ")");
				} else {
					screen.setCurrentFillColor(newColor);
					statusBar.setText("채우기 색상 선택됨 (R:" + newColor.getRed() + ", G:" + newColor.getGreen() + ", B:" + newColor.getBlue() + ")");
				}
			}
			return;
		}
		
		else {
			if (e.getSource() == toolboxButtons[0]) {
				screen.setDrawMode(Screen.POINT);
				statusBar.setText("점 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[1]) {
				screen.setDrawMode(Screen.LINE);
				statusBar.setText("선 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[2]) {
				screen.setDrawMode(Screen.CIRCLE);
				statusBar.setText("원 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[3]) {
				screen.setDrawMode(Screen.RECTANGLE);
				statusBar.setText("네모 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[4]) {
				screen.setDrawMode(Screen.ERASER); 
				statusBar.setText("지우개 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[5]) {
				screen.setDrawMode(Screen.TEXT); 
				statusBar.setText("텍스트 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[6]) {
				screen.setDrawMode(Screen.SELECTION); 
				statusBar.setText("선택 도구가 선택되었습니다.");
				return;
			}
			else if (e.getSource() == toolboxButtons[7]) {
			    screen.deleteSelection();
			    statusBar.setText("선택된 도형들을 삭제했습니다.");
			    return;
			}
			else if (e.getSource() == toolboxButtons[8]) {
			    screen.setDrawMode(Screen.FILL_BUCKET);
			    statusBar.setText("채우기 도구가 선택되었습니다.");
			    return;
			}

			if (e.getSource() instanceof JMenuItem) {
				if(cmd.equals("점(P)")) {
					screen.setDrawMode(Screen.POINT);
					statusBar.setText("점 도구가 선택되었습니다.");
				} else if(cmd.equals("선(L)")) {
					screen.setDrawMode(Screen.LINE);
					statusBar.setText("선 도구가 선택되었습니다.");
				} else if(cmd.equals("원(C)")) {
					screen.setDrawMode(Screen.CIRCLE);
					statusBar.setText("원 도구가 선택되었습니다.");
				} else if(cmd.equals("네모(B)")) {
					screen.setDrawMode(Screen.RECTANGLE);
					statusBar.setText("네모 도구가 선택되었습니다.");
				}
				return; 
			}
			
			for(int i=0; i < this.colorButtons.length; i++) {
				if(colorButtons[i].equals(e.getSource())) {
					Color selected = this.colors[i];
					currentSelectedColorButton.setBackground(selected);
					
					if (currentSelectedColorButton == lineColorButton) {
						screen.setCurrentColor(selected);
						this.statusBar.setText("선 색상 선택됨 (R:" + selected.getRed() + ", G:" + selected.getGreen() + ", B:" + selected.getBlue() + ")");
					} else {
						screen.setCurrentFillColor(selected);
						this.statusBar.setText("채우기 색상 선택됨 (R:" + selected.getRed() + ", G:" + selected.getGreen() + ", B:" + selected.getBlue() + ")");
					}
					
					break;
				}
			}
		}
		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	    if (e.getSource() == strokeSpinner) {
	        int newStroke = (int) strokeSpinner.getValue();
	        screen.setCurrentStroke(newStroke);
	        statusBar.setText("선 두께가 " + newStroke + "으로 변경되었습니다.");
	    }
	    else if (e.getSource() == eraserSpinner) {
	        int newEraserSize = (int) eraserSpinner.getValue();
	        screen.setEraserSize(newEraserSize);
	        statusBar.setText("지우개 크기가 " + newEraserSize + "로 변경되었습니다.");
	    }
	    else if (e.getSource() == fontSizeSpinner) {
	        int newFontSize = (int) fontSizeSpinner.getValue();
	        screen.setCurrentFontSize(newFontSize);
	        statusBar.setText("폰트 크기가 " + newFontSize + "로 변경되었습니다.");
	    }
	}
}