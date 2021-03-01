package com.example.beneficialownerpracticerussia;

// класс описывающий результаты кейсов
public class cases {
    //значения кейсов
    String caseName;
    String caseResult;
    String typeOfPayment;
    String caseReference;

    // конструктор
    public cases(String caseName, String caseResult, String typeOfPayment, String caseReference){
        this.caseName = caseName;
        this.caseResult = caseResult;
        this.typeOfPayment = typeOfPayment;
        this.caseReference = caseReference;
    }

    //геттеры
    public String getCaseName(){return caseName;}
    public String getCaseResult(){return caseResult;}
    public String getTypeOfPayment(){return typeOfPayment;}
    public String getCaseReference(){return caseReference;}
}