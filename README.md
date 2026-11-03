# F.CSA313 — Lab 08: Mockito Unit Testing

**Оюутан:** Ц.Бэлгүтэй  
**Оюутны код:** B232270053  
**Хичээл:** F.CSA313 — Software Quality Assurance and Testing  
**Лаборатори:** Lab 08 — Mockito

## 1. Лабораторийн зорилго

Энэхүү лабораторийн ажлаар Mockito ашиглан Java application-ийн unit test бичиж, гаднын хамаарлуудыг mock object-оор орлуулах, method-ийн үр дүн болон харилцан үйлчлэлийг шалгах дадлага хийв.

Тестийн объект нь `CourseRegistrationService` бөгөөд дараах хамаарлуудтай:

- `StudentRepository` — оюутан, хичээл болон бүртгэлийн мэдээлэлтэй ажиллана.
- `EmailSender` — бүртгэл амжилттай болсны дараа имэйл илгээнэ.

Тестийн үед эдгээр хамаарлыг Mockito-ийн `@Mock` ашиглан орлуулсан.

## 2. Ашигласан технологи

| Технологи | Хувилбар |
|---|---|
| Java | 23.0.2 |
| Apache Maven | 3.9.9 |
| JUnit Jupiter | 5.10.2 |
| Mockito | 5.11.0 |
| Maven Surefire | 3.2.5 |
| Үйлдлийн систем | macOS 14.5 |

Хувилбарын мэдээллийг `results/java-version.txt` болон `results/maven-version.txt` файлуудад хадгалсан.

## 3. Төслийн бүтэц

```text
batalgaa-lab-08/
├── pom.xml
├── README.md
├── .gitignore
├── src/
│   ├── main/
│   │   └── java/mn/edu/must/sqat/
│   │       ├── Student.java
│   │       ├── StudentRepository.java
│   │       ├── EmailSender.java
│   │       └── CourseRegistrationService.java
│   └── test/
│       └── java/mn/edu/must/sqat/
│           └── CourseRegistrationServiceTest.java
└── results/
    ├── java-version.txt
    ├── maven-version.txt
    ├── mvn-test.txt
    ├── mvn-test-mutant-1.txt
    ├── mvn-test-mutant-2.txt
    └── mvn-test-mutant-3.txt
