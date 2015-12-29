import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javaxt.io.Image;
import net.sf.jmimemagic.Magic;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class Main
{
  private static final long NEW_FILES_CHECK_DELAY = 3000;
  private static final long IMAGE_SWITCH_DELAY = 5000;

  public static List<String> queue;
  public static List<String> pending;

  public static Image image;

  public static int currentIndex;

  public static ImagePanel imagePanel;

  public static Timer imageTimer;
  public static Timer newFilesTimer;

  public static FTPClient ftpClient;

  public static boolean isReadingFTP = false;

  public static final String LOCAL_IMAGES_PATH = "images";

  public static final String FTP_HOST = "ftp.jhdemoane.be";
  public static final int FTP_PORT = 21;
  public static final String FTP_IMAGES_PATH = "upload/test";

  public static final String FTP_ERROR_PATH = "error";

  public static final String FTP_USER = "jhdemoane.be";
  public static final String FTP_PASSWORD = "jhdemoane";

  public static void main(String[] pArgs) throws Exception
  {
    // make sure we have a local folder to store received images.
    createLocalImagesDirectory();

    // delete local folder
    deleteLocalImages();

    // connect ftp.
    ftpClient = connectFTP();

    // receive initial pictures and load them in queue.
    queue = receiveFTP();

    // create a ImagePanel
    createImagePanel();

    // start showing pictures
    showFirstPicture();

    // start imageTimer for showing pictures
    startImageTimer();

    // start imageTimer for pulling new pictures.
    startNewFilesTimer();
  }

  private static void deleteLocalImages()
  {
    try
    {
      FileUtils.cleanDirectory(new File(new File("").getAbsoluteFile() + "\\" + LOCAL_IMAGES_PATH));
    }
    catch (IOException e)
    {
      Logger.error(Main.class, "deleteLocalImages", "Could not clean local images folder");
    }
  }

  private static void startNewFilesTimer()
  {
    newFilesTimer = new Timer();
    newFilesTimer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        Logger.debug(Main.class, "startNewFilesTimer", "Check for new Files");
        checkNewFiles();
        // schedule timer again.
        startNewFilesTimer();
      }
    }, NEW_FILES_CHECK_DELAY);
  }

  private static void startImageTimer()
  {
    imageTimer = new Timer();
    imageTimer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        Logger.debug(Main.class, "startImageTimer", "Next Image!");
        showNextPicture();
        // schedule image timer
        startImageTimer();
      }
    }, IMAGE_SWITCH_DELAY);
  }

  private static void checkNewFiles()
  {
    Logger.debug(Main.class, "checkNewFiles", "Start reading FTP");

    if (pending == null) pending = new ArrayList<String>();

    List<String> tmpPending = receiveFTP();
    if (tmpPending == null) return;

    for (String file : tmpPending)
    {
      if ((queue != null && queue.contains(file)) || pending.contains(file)) continue;

      // added new file!
      Logger.debug(Main.class, "checkNewFiles", "Added new file: " + file);
      pending.add(file);
    }

    Logger.debug(Main.class, "checkNewFiles", "Stop reading FTP");
  }

  public static void createImagePanel()
  {
    imagePanel = new ImagePanel();
  }

  private static void createLocalImagesDirectory()
  {
    File dir = new File(new File("").getAbsoluteFile() + "\\" + LOCAL_IMAGES_PATH);
    boolean succesExecutable = dir.setExecutable(true, false);
    boolean succesReadable = dir.setReadable(true, false);
    boolean succesWritable = dir.setWritable(true, false);
    boolean success = dir.mkdir();

    if (!succesExecutable)
    {
      Logger.warning(Main.class, "createLocalImagesDirectory", "Unable to set dir Executable!");
    }

    if (!succesReadable)
    {
      Logger.warning(Main.class, "createLocalImagesDirectory", "Unable to set dir Readable!");
    }

    if (!succesWritable)
    {
      Logger.warning(Main.class, "createLocalImagesDirectory", "Unable to set dir Writable!");
    }

    if (!success)
    {
      Logger.warning(Main.class, "createLocalImagesDirectory", "Directory not created!");
    }
  }

  private static List<String> receiveFTP()
  {
    if (isReadingFTP)
    {
      Logger.warning(Main.class, "receiveFTP", "Still reading previous session");
      return null;
    }

    List<String> tmpFileList = null;

    try
    {
      isReadingFTP = true;

      // lists files and directories in the current working directory
      FTPFile[] files = ftpClient.listFiles(FTP_IMAGES_PATH);

      // start processing files
      File downloadFile;
      OutputStream output;

      for (FTPFile file : files)
      {
        String details = file.getName();
        if (file.isDirectory())
        {
          continue;
        }

        // create new array if necessary.
        if (tmpFileList == null) tmpFileList = new ArrayList<String>();

        // store file locally
        downloadFile = new File(new File("").getAbsoluteFile() + "\\" + LOCAL_IMAGES_PATH + "\\" + file.getName());
        if (!downloadFile.exists())
        {
          Logger.debug(Main.class, "receiveFTP", "Downloaded new file: " + file);
          output = new BufferedOutputStream(new FileOutputStream(downloadFile));
          ftpClient.retrieveFile(FTP_IMAGES_PATH + "/" + file.getName(), output);
          output.close();
        }


        if (checkIsImage(downloadFile))
        {
          // add filename to queue
          tmpFileList.add(details);
        }
        else
        {
          // move file to error folder
          ftpClient.makeDirectory(FTP_IMAGES_PATH + "/" + FTP_ERROR_PATH);
          ftpClient.rename(FTP_IMAGES_PATH + "/" + file.getName(), FTP_IMAGES_PATH + "/" + FTP_ERROR_PATH + "/" + file.getName());

          // delete local file
          boolean delete = downloadFile.delete();
          if (!delete) Logger.warning(Main.class, "receiveFTP", "Deletion for file + " + file.getName() + " not succeeded");
        }
      }
    }
    catch (Exception e)
    {
      Logger.error(Main.class, "receiveFTP", e.getMessage());
    }
    finally
    {
      isReadingFTP = false;
    }

    return tmpFileList;
  }

  private static FTPClient connectFTP() throws IOException
  {
    Logger.debug(Main.class, "connectFTP", "entry");
    FTPClient ftpClient = new FTPClient();
    ftpClient.connect(FTP_HOST, FTP_PORT);
    ftpClient.login(FTP_USER, FTP_PASSWORD);
    ftpClient.enterLocalPassiveMode();
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    Logger.debug(Main.class, "connectFTP", "exit");
    return ftpClient;
  }

  public static void showFirstPicture()
  {
    // load first image.
    loadImage(0);

    // show it on panel.
    showImage();
  }

  public static void loadImage(int pIndex)
  {
    try
    {
      if (queue == null || queue.isEmpty()) return;

      // get new Image
      String imagePath = queue.get(pIndex);
      image = new Image(LOCAL_IMAGES_PATH + "/" + imagePath);
      image.resize(imagePanel.getImageMaxWidth(), imagePanel.getImageMaxHeight(), true);
      image.rotate();
    }
    catch (Exception e)
    {
      Logger.error(Main.class, "loadImage", e.getMessage());
    }
  }

  public static void showImage()
  {
    imagePanel.setImage(image);
  }

  public static void showNextPicture()
  {
    currentIndex++;

    if (queue != null)
    {
      // check for end of loop.
      if (currentIndex >= queue.size()) currentIndex = 0;
    }
    else
    {
      // queue is null?
      currentIndex = 0;
      queue = new ArrayList<String>();
    }

    if (pending != null && !pending.isEmpty())
    {
      String newFile = pending.remove(0);
      queue.add(currentIndex, newFile);
    }

    // get new Image
    loadImage(currentIndex);

    // show it on panel.
    showImage();
  }

  public static boolean checkIsImage(File pFile)
  {
    try
    {
      String mimeType = Magic.getMagicMatch(pFile, false, false).getMimeType();
      if (!mimeType.startsWith("image/"))
      {
        Logger.debug(Main.class, "checkIsImage", "File is not a imgage : " + pFile.getName());
        return false;
      }
      return true;
    }
    catch (Exception e)
    {
      Logger.error(Main.class, "checkIsImage", "File : " + pFile.getName() + " | " + e.getClass() + " " + e.getMessage());
      return false;
    }
  }

  public static int getPendingCount()
  {
    return pending != null ? pending.size() : 0;
  }

  public static int getQueueCount()
  {
    return queue != null ? queue.size() : 0;
  }

  public static int getCurrentPicture()
  {
    return currentIndex + 1;
  }
}
