/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import com.alibaba.cloud.faceengine.*;
import com.alibaba.cloud.faceengine.Error;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.lang.reflect.Field;

public class App {
    private static final String VENDOR_KEY = "eyJ2ZW5kb3JJZCI6ImNlc2hpX3ZlbmRvciIsInJvbGUiOjIsImNvZGUiOiIzRDE5RUIwNjY1OEE5MUExQzlCNDY0MzhDN0QwNDFGMyIsImV4cGlyZSI6IjIwMTkwMzMxIiwidHlwZSI6MX0=";
    private static String PICTURE_ROOT = "./pictures/";
    private static int RunMode = Mode.TERMINAL;
    //private static int RunMode = Mode.CLOUD;
    private static String GROUP_NAME = "组b的粉丝地方";
    private static Group sGroup = new Group();

    public static void main(String[] args) {
        //step 1: authorize or enable debug
        FaceEngine.enableDebug(false);
        System.out.println("VENDOR_KEY : " + VENDOR_KEY);
        int error = FaceEngine.authorize(VENDOR_KEY);
        if (error != Error.OK) {
            System.out.println("authorize error : " + error);
            return;
        } else {
            System.out.println("authorize OK");
        }


        //step 2: set Cloud addr and account if you using CloudServer
        //FaceEngine.setCloudAddr("101.132.89.177", 15000);
        //FaceEngine.setCloudAddr("127.0.0.1", 8080);
        FaceEngine.setCloudLoginAccount("admin", "admin");


        //face detect
        detectPicture();


        //1:1 face verify
        verifyPicture();


        //1:N face recognize
        FaceRecognize faceRecognize = FaceRecognize.createInstance(RunMode);
        FaceRegister faceRegister = FaceRegister.createInstance();

        //1:N, step1:register face
        registerPictures(faceRegister);
        //1:N, step2:register face
        recognizePictures(faceRecognize, sGroup.id);


        //other face database management
        getAllGroups(faceRegister);
        getGroupInfo(faceRegister, sGroup.id);
        getAllPersons(faceRegister, sGroup.id);
        getPersonNum(faceRegister, sGroup.id);


        //release instance
        FaceRecognize.deleteInstance(faceRecognize);
        FaceRegister.deleteInstance(faceRegister);
        faceRecognize = null;
        faceRegister = null;
    }


    private static void detectPicture() {
        System.out.println("detectPicture begin");

        //detectPicture step1: create FaceDetect, mode type can be TERMINAL or CLOUD
        FaceDetect faceDetect = FaceDetect.createInstance(RunMode);
        if (faceDetect == null) {
            System.out.println("FaceDetect.createInstance error");
            return;
        }

        byte[] jpegData = loadFile(PICTURE_ROOT + "many_faces.jpg");
        //byte[] jpegData = loadFile(PICTURE_ROOT + "fanbingbing_with_glass.jpg");
        Image image = new Image();
        image.data = jpegData;
        image.format = ImageFormat.JPEG;

        //detectPicture step2: set picture detect parameter
        DetectParameter pictureDetectParameter = faceDetect.getPictureParameter();
        pictureDetectParameter.checkQuality = 1;
        pictureDetectParameter.checkLiveness = 1;
        pictureDetectParameter.checkAge = 1;
        pictureDetectParameter.checkGender = 1;
        pictureDetectParameter.checkExpression = 1;
        pictureDetectParameter.checkGlass = 1;
        faceDetect.setPictureParameter(pictureDetectParameter);

        //detectPicture step3: detect picture
        Face[] faces = faceDetect.detectPicture(image);

        if (faces == null) {
            System.out.println("detectPicture faces number:0");
        } else {
            System.out.println("detectPicture faces number:" + faces.length);
            for (int i = 0; i < faces.length; i++) {
                System.out.println("detectPicture faces[" + i + "]:" + faces[i]);
            }
        }

        //detectPicture step4: delete FaceDetect instance
        FaceDetect.deleteInstance(faceDetect);
        faceDetect = null;
        System.out.println("detectPicture end\n\n======================");
    }

    private static void verifyPicture() {
        System.out.println("verifyPicture begin");

        //verifyPicture step1: create FaceVerify, mode type can be TERMINAL or CLOUD
        FaceVerify faceVerify = FaceVerify.createInstance(RunMode);
        if (faceVerify == null) {
            System.out.println("FaceVerify.createInstance error");
            return;
        }
        FaceDetect faceDetect = FaceDetect.createInstance(RunMode);
        if (faceDetect == null) {
            System.out.println("FaceDetect.createInstance error");
            return;
        }

        byte[] imageData1 = loadFile(PICTURE_ROOT + "liudehua_feature1.jpg");
        Image image1 = new Image();
        image1.data = imageData1;
        image1.format = ImageFormat.ImageFormat_UNKNOWN;

        byte[] imageData2 = loadFile(PICTURE_ROOT + "liudehua_feature2.jpg");
        Image image2 = new Image();
        image2.data = imageData2;
        image2.format = ImageFormat.ImageFormat_UNKNOWN;

        Face[] faces1 = faceDetect.detectPicture(image1);
        Face[] faces2 = faceDetect.detectPicture(image2);

        //score >=70
        VerifyResult verifyResult[] = faceVerify.verifyPicture(image1, faces1[0], image2, faces2);
        if (verifyResult == null) {
            System.out.println("verifyPicture result number:0");
        } else {
            System.out.println("verifyPicture result number:" + verifyResult.length);
            for (int i = 0; i < verifyResult.length; i++) {
                System.out.println("verifyPicture result [" + i + "]:" + verifyResult[i]);
            }
        }

        FaceVerify.deleteInstance(faceVerify);
        FaceDetect.deleteInstance(faceDetect);
        System.out.println("verifyPicture end\n\n======================");
    }

    private static final String BASE_PERSONS[] = {
            "liudehua_feature1.jpg", "liudehua_feature2.jpg",
            "zhangxueyou_feature1.jpg", "zhangxueyou_feature2.jpg"};
    private static final String TEST_PERSONS[] = {
            "liudehua.jpg",
            "zhangxueyou.jpg"};

    private static void registerPictures(FaceRegister faceRegister) {
        System.out.println("registerPictures begin");
        sGroup.name = GROUP_NAME;
        sGroup.modelType = ModelType.MODEL_SMALL;
        int error = faceRegister.createGroup(sGroup);
        if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
            throw new RuntimeException("createGroup " + GROUP_NAME + " error:" + error);
        } else {
            System.out.println("createGroup OK:" + error + " groupId:" + sGroup.id);
        }

        addPersonsAndFeatures(faceRegister, sGroup.id);
        System.out.println("registerPictures end\n\n======================");
    }

    private static void addPersonsAndFeatures(FaceRegister faceRegister, String groupId) {
        FaceDetect faceDetect = FaceDetect.createInstance(RunMode);
        if (faceDetect == null) {
            System.out.println("FaceDetect.createInstance error");
            return;
        }

        for (int i = 0; i < BASE_PERSONS.length; i++) {
            String personName = BASE_PERSONS[i].split("_")[0];
            String featureName = BASE_PERSONS[i].split("_")[1].split("\\.")[0];

            byte[] imageData = loadFile(PICTURE_ROOT + BASE_PERSONS[i]);
            if (imageData == null) {
                throw new RuntimeException("loadFile " + BASE_PERSONS[i] + " error");
            }

            Image image = new Image();
            image.data = imageData;
            image.format = ImageFormat.ImageFormat_UNKNOWN;
            Face faces[] = faceDetect.detectPicture(image);
            if (faces == null) {
                throw new RuntimeException("detectPicture " + BASE_PERSONS[i] + " error");
            }


            String featureStr = faceRegister.extractFeature(image, faces[0], ModelType.MODEL_SMALL);
            if (featureStr == null) {
                throw new RuntimeException("extractFeature " + BASE_PERSONS[i] + " error");
            }


            Person person = new Person();
            person.name = personName;
            int error = faceRegister.addPerson(groupId, person);
            if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
                throw new RuntimeException("addPerson " + personName + " error:" + error);
            } else {
                System.out.println("addPerson success: personName:" + person.name + " personId:" + person.id);
            }


            Feature feature = new Feature();
            feature.name = featureName;
            feature.feature = featureStr;
            error = faceRegister.addFeature(person.id, feature);
            if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
                throw new RuntimeException("addFeature " + featureName + " error:" + error);
            } else {
                System.out.println("addFeature success: personName:" + personName + " featureId:" + feature.id + " featureName:" + feature.name);/**/
            }
        }

        FaceDetect.deleteInstance(faceDetect);
    }

    private static void recognizePictures(FaceRecognize faceRecognize, String groupId) {
        FaceDetect faceDetect = FaceDetect.createInstance(RunMode);
        if (faceDetect == null) {
            System.out.println("FaceDetect.createInstance error");
            return;
        }

        faceRecognize.setGroupId(groupId);

        for (int i = 0; i < TEST_PERSONS.length; i++) {
            String personName = TEST_PERSONS[i].split("\\.")[0];

            byte[] imageData = loadFile(PICTURE_ROOT + TEST_PERSONS[i]);
            if (imageData == null) {
                throw new RuntimeException("loadFile " + TEST_PERSONS[i] + " error");
            }

            Image image = new Image();
            image.data = imageData;
            image.format = ImageFormat.ImageFormat_UNKNOWN;
            Face faces[] = faceDetect.detectPicture(image);
            if (faces == null) {
                throw new RuntimeException("detectPicture " + TEST_PERSONS[i] + " error");
            }

            RecognizeResult recognizeResults[] = faceRecognize.recognizePicture(image, faces);
            if (recognizeResults == null) {
                System.out.println("recognizePicture error");
            } else {
                if (recognizeResults[0].personName.equals(personName)) {
                    System.out.println("recognizePicture OK, " + personName + " vs " + recognizeResults[0]);
                } else {
                    System.out.println("recognizePicture Fail, " + personName + " vs " + recognizeResults[0]);
                }
            }
        }

        FaceDetect.deleteInstance(faceDetect);
    }


    private static void getAllGroups(FaceRegister faceRegister) {
        System.out.println("getAllGroups begin");
        Group groupInfos[] = faceRegister.getAllGroups();
        if (groupInfos == null) {
            System.out.println("getAllGroupInfos : null");
        } else {
            for (int i = 0; i < groupInfos.length; i++) {
                System.out.println("getAllGroupInfos [" + i + "]=" + groupInfos[i]);
            }
        }
        System.out.println("getAllGroups end\n\n======================");
    }

    private static void getGroupInfo(FaceRegister faceRegister, String groupId) {
        System.out.println("getGroup begin");
        Group groupInfo = faceRegister.getGroup(groupId);
        System.out.println("getGroupInfo=" + groupInfo);
        System.out.println("getGroup end\n\n======================");
    }


    private static void getAllPersons(FaceRegister faceRegister, String groupId) {
        System.out.println("getAllPersons begin+++++++++++");
        Person persons[] = faceRegister.getAllPersons(groupId);
        if (persons == null) {
            System.out.println("getAllPersons =null");
        } else {
            for (int i = 0; i < persons.length; i++) {
                System.out.println("getAllPersons [" + i + "]=" + persons[i]);
            }
        }

        System.out.println("getAllPersons end\n\n------------");
    }

    private static void getPersonNum(FaceRegister faceRegister, String groupId) {
        System.out.println("getPersonNum begin+++++++++++");
        int num = faceRegister.getPersonNum(groupId);
        System.out.println("getPersonNum=" + num);
        System.out.println("getPersonNum end\n\n------------");
    }


    private static byte[] loadFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("file not exist:" + filePath);
            return null;
        }

        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }

        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        try {
            FileInputStream fi = new FileInputStream(file);
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            fi.close();
        } catch (IOException e) {
        }

        if (offset != buffer.length) {
            System.out.println("Could not completely read file");
            return null;
        }

        return buffer;
    }


    private static void setAliFaceEngineLibPath() {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String userDir = System.getProperty("user.dir");
        System.out.println("os.name: " + osName + ", os.arch: " + osArch + ", user.dir: " + userDir);

        if (osName == null) {
            throw new RuntimeException("setAliFaceEngineLibPath error, unknown os");
        }

        String AliFaceEngineLibPath = "";
        if (osName.contains("Mac")) {
            AliFaceEngineLibPath = "libs/Darwin/";
        } else if (osName.contains("Windows")) {
            AliFaceEngineLibPath = "libs/Windows/";
            if (osArch.contains("64")) {
                AliFaceEngineLibPath += "x64/";
            } else {
                AliFaceEngineLibPath += "x86/";
            }
        } else {
            throw new RuntimeException("setAliFaceEngineLibPath error, unsupported os");
        }

        System.setProperty("java.library.path", System.getProperty("java.library.path")
                + ":" + userDir + "/" + AliFaceEngineLibPath);
        System.out.println("java.library.path: " + System.getProperty("java.library.path"));

        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("setAliFaceEngineLibPath error, set java.library.path error");
        }
    }

    static {
        setAliFaceEngineLibPath();
    }
}
