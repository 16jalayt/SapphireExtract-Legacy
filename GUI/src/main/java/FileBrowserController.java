import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sapphireforge.program.ParseInput;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class FileBrowserController
{
    private static final Logger logger = ParseInput.logger;

    @FXML private TreeView treevew;

    public FileBrowserController()
    {
        logger.info("Fatal error!");
        logger.warn("oops.");
        logger.fatal("There is no error... my bad.");

        logger.trace("We've just greeted the user!");
        logger.debug("We've just greeted the user!");
        logger.info("We've just greeted the user!");
        logger.warn("We've just greeted the user!");
        logger.error("We've just greeted the user!");
        logger.fatal("We've just greeted the user!");
    }

    @FXML
    private void initialize()
    {
        System.out.println("initializing");

        /*TreeItem<String> rootItem = new TreeItem<String> ("Inbox");
        treevew.setRoot(rootItem);
        //rootItem.setExpanded(true);

        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<String> ("Message" + i);
            rootItem.getChildren().add(item);
        }*/
    }

    @FXML
    private void printOutput()
    {
        System.out.println("button works!");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("potato");
        alert.setHeaderText("Information Alert");
        String s ="This is an example of JavaFX 8 Dialogs... ";
        alert.setContentText(s);
        alert.show();
        //blocking?
        //alert.showAndWait();
    }

    //Just clear the pane
    @FXML
    private void NewFile()
    {
        initialize();
    }

    //Use open multiple and create root for each file selected
    @FXML
    private void OpenFile()
    {
        FileChooser chooser = new FileChooser();
        //text is just "Open" by default
        chooser.setTitle("Open File");

        //File returned = chooser.showOpenDialog(new Stage());
        //System.out.println("File Chosen was: "+returned.getName());

        List<File> retList = chooser.showOpenMultipleDialog(null);
        //System.out.println(Arrays.toString(retList.toArray()));
        if (retList == null)
        {
            //error no file. exited out of?
            //log error and do nothing, mabe clear?
            //...
            System.out.println("No file was selected");
            initialize();
            return;
        }

        for (File file : retList)
        {
            //System.out.println(file);
            //TODO: temporary until redone functions
            //TODO:make a seperate thread with progress
            ParseInput.parseFile(file);
        }
    }
}
