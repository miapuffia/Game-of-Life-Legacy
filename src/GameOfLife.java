import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameOfLife extends Application {
	boolean gameLoopRunning = false;
	
	static GraphicsContext gc;
	int numberOfSquares = 40;
	int sizeOfSquares = 15;
	boolean[][] matrix = new boolean[numberOfSquares][numberOfSquares];
	boolean running = false;
	boolean runOnce = false;
	int msInterval = 1000;
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		AnimationTimer gameLoop = new AnimationTimer() {
			private long lastUpdate = 0;
			
			public void handle(long now) {
				if((now - lastUpdate) < msInterval * 1_000_000) {
					return;
				}
				
				boolean[][] newMatrix = new boolean[numberOfSquares][numberOfSquares];
				
				for(int i = 0; i < numberOfSquares; i++) {
					for(int j = 0; j < numberOfSquares; j++) {
						int numNeighbors = numNeighbors(i, j);
						
						if(matrix[i][j] == true) {
							if(numNeighbors == 2 || numNeighbors == 3) {
								newMatrix[i][j] = true;
								gc.setFill(Color.GOLD);
								gc.fillRect(1 + (i * sizeOfSquares), 1 + (j * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
							} else {
								gc.setFill(Color.LIGHTGREY);
								gc.fillRect(1 + (i * sizeOfSquares), 1 + (j * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
							}
						} else {
							if(numNeighbors == 3) {
								newMatrix[i][j] = true;
								gc.setFill(Color.GOLD);
								gc.fillRect(1 + (i * sizeOfSquares), 1 + (j * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
							} else {
								gc.setFill(Color.LIGHTGREY);
								gc.fillRect(1 + (i * sizeOfSquares), 1 + (j * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
							}
						}
					}
				}
				
				matrix = newMatrix;
				
				if(runOnce) {
					runOnce = false;
					stop();
				}
				
				lastUpdate = now;
			}
		};
		
		Canvas canvas = new Canvas(1 + (numberOfSquares * sizeOfSquares), 1 + (numberOfSquares * sizeOfSquares));
		
		gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMouseClicked(e -> {
			if(!running) {
				int x = (int) Math.floor(e.getSceneX() / sizeOfSquares);
				int y = (int) Math.floor(e.getSceneY() / sizeOfSquares);
				
				if(matrix[x][y] == true) {
					gc.setFill(Color.LIGHTGREY);
					gc.fillRect(1 + (x * sizeOfSquares), 1 + (y * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
					
					matrix[x][y] = false;
				} else {
					gc.setFill(Color.GOLD);
					gc.fillRect(1 + (x * sizeOfSquares), 1 + (y * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
					
					matrix[x][y] = true;
				}
			}
		});

		gc.setFill(Color.LIGHTGRAY);
		
		for(int i = 0; i < numberOfSquares; i++) {
			for(int j = 0; j < numberOfSquares; j++) {
				gc.fillRect(1 + (i * sizeOfSquares), 1 + (j * sizeOfSquares), sizeOfSquares - 1, sizeOfSquares - 1);
			}
		}
		
		StackPane mainStackPane = new StackPane(canvas);
		
		Button startButton = new Button("Start");
		Button nextButton = new Button("Next");
		Button rulesButton = new Button("Rules?");
		
		startButton.setOnAction(e -> {
			if(gameLoopRunning) {
				running = false;
				
				gameLoop.stop();
				
				startButton.setText("Start");
				
				nextButton.setDisable(false);
				
				gameLoopRunning = false;
			} else {
				running = true;
				
				gameLoop.start();
		        
		        startButton.setText("Stop");
		        
		        nextButton.setDisable(true);
		        
		        gameLoopRunning = true;
			}
		});
		
		nextButton.setOnAction(e -> {
			if(!gameLoopRunning) {
				runOnce = true;
				
				gameLoop.start();
			}
		});
		
		Label sliderLabel = new Label("Time to step in ms: ");
		
		Slider slider = new Slider(50, 1000, 1000);
		slider.setMajorTickUnit(50);
		slider.setBlockIncrement(50);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setSnapToTicks(true);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> source, Number oldValue, Number newValue) {
				msInterval = newValue.intValue();
			}
		});
		
		rulesButton.setOnAction(e -> {
			QuickAlert.show(AlertType.INFORMATION, "Game of Life rules:", "• Any live cell with less than 2 live neighbors dies, as if by underpopulation.\n• Any live cell with 2 or 3 live neighbours lives on to the next generation.\n• Any live cell with more than 3 live neighbours dies, as if by overpopulation.\n• Any dead cell with exactly 3 live neighbours becomes a live cell, as if by reproduction.");
		});
		
		HBox buttonsHBox = new HBox(20, sliderLabel, slider, startButton, nextButton, rulesButton);
		buttonsHBox.setAlignment(Pos.CENTER);
		
		VBox mainVBox = new VBox(mainStackPane, buttonsHBox);
		
		Scene scene = new Scene(mainVBox);
		
		QuickAlert.show(AlertType.WARNING, "This program is not very stable", "The program may become unresponseive, crash, or freeze. Please keep that in mind and enjoy.");
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Game of Life");
		primaryStage.getIcons().add(new Image(getClass().getResource("/icon.png").toString()));
		primaryStage.show();
	}
	
	private int numNeighbors(int x, int y) {
		int numNeightbors = 0;
		
		if(
			x < 0
			|| x > numberOfSquares
			|| y < 0
			|| y > numberOfSquares
		) {
			System.out.println("Invalid matrix index");
			return 0;
		}
		
		//top left
		if(
			x > 0
			&& y > 0
			&& matrix[x - 1][y - 1] == true
		) {
			numNeightbors++;
		}
		
		//top
		if(
			y > 0
			&& matrix[x][y - 1] == true
		) {
			numNeightbors++;
		}
		
		//top right
		if(
			x < numberOfSquares - 1
			&& y > 0
			&& matrix[x + 1][y - 1] == true
		) {
			numNeightbors++;
		}
		
		//right
		if(
			x < numberOfSquares - 1
			&& matrix[x + 1][y] == true
		) {
			numNeightbors++;
		}
		
		//bottom right
		if(
			x < numberOfSquares - 1
			&& y < numberOfSquares - 1
			&& matrix[x + 1][y + 1] == true
		) {
			numNeightbors++;
		}
		
		//bottom
		if(
			y < numberOfSquares - 1
			&& matrix[x][y + 1] == true
		) {
			numNeightbors++;
		}
		
		//bottom left
		if(
			x > 0
			&& y < numberOfSquares - 1
			&& matrix[x - 1][y + 1] == true
		) {
			numNeightbors++;
		}
		
		//left
		if(
			x > 0
			&& matrix[x - 1][y] == true
		) {
			numNeightbors++;
		}
		
		return numNeightbors;
	}
}
