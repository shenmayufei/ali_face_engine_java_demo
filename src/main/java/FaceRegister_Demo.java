/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import com.alibaba.cloud.faceengine.Error;
import com.alibaba.cloud.faceengine.*;

public class FaceRegister_Demo {
    private static int RunMode = Mode.TERMINAL;
    //private static int RunMode = Mode.CLOUD;
    private static String GROUP_NAME = "TEST3";
    private static Group sGroup = new Group();

    public static void main(String[] args) {
        //step 1: authorize or enable debug
        FaceEngine.enableDebug(false);
        System.out.println("VENDOR_KEY : " + Utils.VENDOR_KEY);
        int error = FaceEngine.authorize(Utils.VENDOR_KEY);
        if (error != Error.OK) {
            System.out.println("authorize error : " + error);
            return;
        } else {
            System.out.println("authorize OK");
        }


        //step 2: set Cloud addr and account if you using CloudServer
        //FaceEngine.setCloudAddr("101.132.89.177", 15000);
        //FaceEngine.setCloudAddr("127.0.0.1", 8080);
        FaceEngine.setCloudLoginAccount("user_register", "666666");


        //1:N face recognize
        FaceRegister faceRegister = FaceRegister.createInstance();

        //1:N, step1:register face
        registerPictures(faceRegister);

        updatePersonByName(faceRegister, sGroup.id);

        //other face database management
        getAllGroups(faceRegister);
        getGroupInfo(faceRegister, sGroup.id);
        getAllPersons(faceRegister, sGroup.id);
        getPersonNum(faceRegister, sGroup.id);

        updatePerson(faceRegister, sGroup.id);

        updatePersonByName(faceRegister, sGroup.id);

        copyGroup(faceRegister);

        //release instance
        FaceRegister.deleteInstance(faceRegister);
    }


    private static final String BASE_PERSONS[] = {
            "liudehua_feature1.bmp", "liudehua_feature2.bmp",
            "zhangxueyou_feature1.bmp", "zhangxueyou_feature2.bmp"};
    private static final String TEST_PERSONS[] = {
            "liudehua.bmp",
            "zhangxueyou.bmp"};

    private static void registerPictures(FaceRegister faceRegister) {
        System.out.println("registerPictures begin");
        sGroup.name = GROUP_NAME;
        sGroup.modelType = ModelType.MODEL_3K;
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

            byte[] imageData = Utils.loadFile(Utils.PICTURE_ROOT + BASE_PERSONS[i]);
            if (imageData == null) {
                throw new RuntimeException("loadFile " + BASE_PERSONS[i] + " error");
            }

            Image image = new Image();
            image.data = imageData;
            image.format = ImageFormat.COMPRESSED;

            Person person = new Person();
            person.name = personName;
            person.tag = "mytag";

            int error = faceRegister.registerPicture(groupId, image, person, featureName);
            if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
                throw new RuntimeException("registerPicture " + personName + " error:" + error);
            } else {
                System.out.println("registerPicture success: personName:" + person.name + " personId:" + person.id);
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
        System.out.println("getGroup begin:" + groupId);
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

    private static void updatePerson(FaceRegister faceRegister, String groupId) {
        Person persons[] = faceRegister.getAllPersons(groupId);
        if (persons == null) {
            System.out.println("updatePerson getAllPersons =null");
        } else {
            for (int i = 0; i < persons.length; i++) {
                System.out.println("updatePerson before [" + i + "]=" + persons[i]);
                persons[i].tag = "new tag44";
                faceRegister.updatePerson(persons[i].id, persons[i]);
            }
        }

        Person persons2[] = faceRegister.getAllPersons(groupId);

        if (persons2 == null) {
            System.out.println("updatePerson getAllPersons =null");
        } else {
            for (int i = 0; i < persons2.length; i++) {
                System.out.println("updatePerson after [" + i + "]=" + persons2[i]);
            }
        }
    }

    private static void updatePersonByName(FaceRegister faceRegister, String groupId) {
        Person person = faceRegister.getPersonByName(groupId, "liudehua");
        if (person == null) {
            System.out.println("updatePersonByName person is null");
            return;
        }

        System.out.println("updatePersonByName before " + person);
        person.tag = "new tag1234";
        faceRegister.updatePerson(person.id, person);

        Person personsNew = faceRegister.getPerson(person.id);

        if (personsNew == null) {
            System.out.println("updatePersonByName after is null");
        } else {
            System.out.println("updatePersonByName after is " + personsNew);
        }
    }

    private static void copyGroup(FaceRegister faceRegister) {
        System.out.println("copyGroup begin+++++++++++");
        Group newGroup = new Group();
        newGroup.name = "NewGroup";
        newGroup.modelType = sGroup.modelType;
        int error = faceRegister.createGroup(newGroup);
        if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
            throw new RuntimeException("copyGroup " + GROUP_NAME + " error:" + error);
        } else {
            System.out.println("copyGroup OK:" + error + " groupId:" + sGroup.id);
        }

        Person persons[] = faceRegister.getAllPersons(sGroup.id);
        if (persons == null) {
            return;
        }

        for (int i = 0; i < persons.length; i++) {
            Person personCopy = new Person();
            personCopy.name = persons[i].name;
            personCopy.tag = persons[i].tag;
            error = faceRegister.addPerson(newGroup.id, personCopy);
            if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
                throw new RuntimeException("copyGroup addPerson" + personCopy.name + " error:" + error);
            }

            if (persons[i].features == null || persons[i].features.length == 0) {
                continue;
            }

            for (int j = 0; j < persons[i].features.length; j++) {
                Feature feature = new Feature();
                feature.name = persons[i].features[j].name;
                feature.feature = persons[i].features[j].feature;
                error = faceRegister.addFeature(personCopy.id, feature);
                if (error != Error.OK && error != Error.ERROR_EXISTED && error != Error.ERROR_CLOUD_EXISTED_ERROR) {
                    throw new RuntimeException("copyGroup addFeature" + feature.name + " error:" + error);
                }
            }
        }


        System.out.println("copyGroup getAllPersons begin+++++++++++");
        Person personsCopy[] = faceRegister.getAllPersons(newGroup.id);
        if (personsCopy == null) {
            System.out.println("copyGroup getAllPersons personsCopy=null");
        } else {
            for (int i = 0; i < personsCopy.length; i++) {
                System.out.println("copyGroup getAllPersons [" + i + "]=" + personsCopy[i]);
            }
        }

        System.out.println("copyGroup getAllPersons end\n\n------------");
    }
}
