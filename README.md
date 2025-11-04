# WindowPainter (Java Swing)

Java Swing으로 구현한 간단한 그림판 애플리케이션입니다. `Main.java`, `MainFrame.java`, `Screen.java` 3개의 파일로 구성되어 있으며, 툴바/메뉴/상태바와 캔버스(Screen) 상호작용을 통해 점/선/원/사각형 그리기, 채우기, 지우개, 텍스트 입력, 확대/축소, 이미지 변환(좌우/상하/회전), 저장/열기 기능을 제공합니다.

## 주요 기능
- 그리기 도구: 점, 선, 원, 사각형, 지우개, 텍스트
- 색상 선택: 선 색상/채우기 색상 버튼, 팔레트, 컬러 피커 지원
- 채우기: 도형 채우기 토글, 선택 영역 채우기
- 선 두께/지우개 크기/폰트 크기 조절 (스피너)
- 확대/축소(Zoom), 디버그 정보 보기 토글
- 파일 저장/열기(.sav), 이미지 저장/열기(PNG/JPG 등), 이미지 변환(좌우/상하 대칭, 90도 회전)

## 파일 구성
- `Main.java`: 애플리케이션 진입점. `new MainFrame()` 호출
- `MainFrame.java`: 프레임/메뉴/툴바/상태바 생성, 액션/체인지 이벤트 처리, 파일/이미지 I/O 트리거
- `Screen.java`: 실제 그리기 로직과 상태 관리. `paintComponent(Graphics)` 렌더링, 마우스 이벤트 처리, `AbstractDrawingObject` 파생 객체 관리

## 실행 방법
Java JDK가 설치된 환경에서 다음과 같이 실행할 수 있습니다.

```bash
javac Main.java MainFrame.java Screen.java
java Main
```

- macOS에서 폰트/한글 표시를 위해 시스템 기본 폰트를 사용합니다.
- 리소스 이미지가 필요한 경우 `resource/` 경로를 프로젝트 루트에 두세요.

## 단축키/메뉴
- 보기 → 확대(⌘+Shift+Z) / 축소(⌘+Shift+X)
- 보기 → 디버그 정보 보기 (체크박스)
- 파일 → 저장/열기(.sav), 이미지 파일로 저장… / 이미지 파일 열기…

## 빌드/의존성
- 표준 Java SE / Swing API만 사용합니다. 별도 외부 라이브러리 없음.

## 기술 스택(상세)
- Java SE 8+ (macOS에서 테스트)
- GUI: Swing(JFrame, JMenuBar, JToolBar, JLabel, JButton, JSpinner, JCheckBox, JFileChooser, JColorChooser 등)
- AWT/2D: AWT 이벤트 모델(ActionListener, ChangeListener, MouseListener, MouseMotionListener), Graphics/Graphics2D, BasicStroke, Color, Point, Rectangle
- 이미지 처리: BufferedImage, ImageIO (PNG/JPG 읽기/쓰기), AffineTransform(확대/축소 스케일 적용)
- 자료구조: `LinkedList<AbstractDrawingObject>` (그림 객체 보관), `LinkedList<Point>/Color` (자유곡선/점 유지)
- 파일 I/O: FileInputStream/FileOutputStream, DataInputStream/DataOutputStream (커스텀 바이너리 포맷 저장/로드)

## 주요 객체/메서드 요약
아래는 실제 코드의 심볼(영문)을 그대로 표기하고, 설명은 한국어로 기술했습니다.

### Main.java
- `main(String[] args)`: 애플리케이션 진입점. `new MainFrame()` 생성.

### MainFrame.java
- `MainFrame()`: 프레임 초기화, `createMenuBar()`, `createToolBar()`, 상태바 생성 및 `Screen` 부착.
- `actionPerformed(ActionEvent)`: 메뉴/툴바/색상 팔레트 클릭 등 UI 이벤트 처리 → `Screen` API 호출(`setDrawMode`, `save`, `open`, `saveImage`, `loadImage`, `applyFlipHorizontal` 등).
- `stateChanged(ChangeEvent)`: `JSpinner` 변경 처리 → 선 두께/지우개 크기/폰트 크기 업데이트(`Screen.setCurrentStroke`, `setEraserSize`, `setCurrentFontSize`).
- `createMenuBar()/createToolBar()/createStatusBar()`: 공용 UI 빌더.
- `setZoom(float)`: 확대/축소 상태를 변경하고 `Screen.setZoomLevel(float)` 위임.
- (옵션) `updateDebugMode()`: 디버그 출력 토글.

### Screen.java
- `paintComponent(Graphics)`: 오프스크린 버퍼를 기반으로 현재 `drawingList`의 `AbstractDrawingObject`들을 그려 최종 화면을 렌더링.
- `setDrawMode(int)`: 현재 도구 모드(POINT/LINE/CIRCLE/RECTANGLE/ERASER/TEXT/FILL_BUCKET) 변경.
- 색상/선/채움 설정: `setCurrentColor(Color)`, `setCurrentFillColor(Color)`, `setCurrentStroke(int)`, `setCurrentFill(boolean)`.
- 기타 상태 설정: `setEraserSize(int)`, `setCurrentFontSize(int)`, `setZoomLevel(float)`, `setDebugMode(boolean)`.
- 편집/선택: `fillSelectedArea(Color)`, `deleteSelection()`, 내부적으로 `currentSelectionRect`/`selectedObjects` 활용.
- 파일 저장/열기: `save(String)`, `open(String)` — `DataOutputStream/DataInputStream`으로 도형 리스트/상태 직렬화.
- 이미지 I/O: `saveImage(File,String)`, `loadImage(File)` — `ImageIO.write/read` 사용.
- 이미지 변환: `applyFlipHorizontal()`, `applyFlipVertical()`, `applyRotate90Degrees(boolean)` → 내부 `performImageTransform(BufferedImage)`로 반영.
- 마우스 인터랙션: `mousePressed/Dragged/Released(MouseEvent)`에서 시작/끝 좌표 관리 및 도형 생성(`new DrawLine/...`).

### AbstractDrawingObject (및 서브클래스)
- 공통 필드: `drawMode`, `startPoint`, `endPoint`, `color`, `stroke`, `isFilled`, `fillColor`.
- 공통 메서드: `draw(Graphics2D)`, `saveToStream(DataOutputStream)`, `contains(Rectangle)`, `isValid()`, `translate(int,int)`, `getDetailedState()`.
- `DrawLine/DrawCircle/DrawRectangle/DrawText`: 각 도형별 `draw(...)`/`saveToStream(...)`/`loadFromStream(...)` 구현.

## 렌더링 파이프라인(요약)
1. 사용자 입력(마우스/키보드) → `MainFrame.actionPerformed` 또는 `Screen.mouse*` 이벤트
2. 도형 생성/상태 변경 → `drawingList` 업데이트
3. `repaint()` 호출 → `Screen.paintComponent(Graphics)` → `Graphics2D`에 순차 렌더링
4. 필요 시 `ImageIO`로 이미지 저장 또는 변환 API 적용

## 저장 포맷(.sav) 개요
- 헤더 없이 `DataOutputStream` 순서대로 객체 수와 각 객체의 필드들을 직렬화
- 각 도형은 `mode, start(x,y), end(x,y), colorRGB, stroke, isFilled, fillColorRGB` 순으로 기록(텍스트는 추가로 문자열/폰트 크기 포함)
- 로딩 시 역순으로 읽어 `AbstractDrawingObject` 서브클래스를 복원

## 라이선스
- MIT License
