package com.getstructure.crm.model

class SOTOnboardingDTO {
    String customerOnboardingId
    String customerId
    String modifiedByEmail
    // data from type form
    String entityName
    String alsoKnownAs
    String workEnvironment
    String differentAddress
    String secureDocumentSoftware
    String secureDocumentURL
    String regulatoryIssues
    String bankName
    String ccCompany
    String financialModelDevelopment
    String numberOfEmployees
    String payrollSoftware
    String numberOfContractors
    String contractorLocation
    String contractorPaymentProcess
    String passwordManager
    // data from discovery call
    String mailManagement
    String businessInsurances
    Boolean registeredAgentService
    String countriesAndStates
    String howSell
    String paymentProcess
    String runwayMrr
    String contractorAgreements
    String employeeAgreements
    String proprietaryInfoAgreements
    Boolean peopleBasedInsurance
    Boolean threeMonthsBanking
    String bankStatementsDue
    Boolean threeMonthsCC
    String ccStatementsDue

    static SOTOnboardingDTO fromTypeFormOnboardRequest(TypeFormOnboardRequest typeFormOnboardRequest ) {
        SOTOnboardingDTO sotOnboardingDTO = new SOTOnboardingDTO()
        if( typeFormOnboardRequest.form_response && typeFormOnboardRequest.form_response.answers ) {
            typeFormOnboardRequest.form_response.answers.each {
                switch( it.field.id ) {

                    // entity name
                    case 'zoXx9y2O6syX':
                        sotOnboardingDTO.entityName = getValueFromAnswer(it)
                        break

                    // work environment
                    case '5OJv4K0VQUwG':
                        sotOnboardingDTO.workEnvironment = getValueFromAnswer(it)
                        break

                    // different address
                    case 'tH6UIUYfNHJO':
                        sotOnboardingDTO.differentAddress = getValueFromAnswer(it)
                        break

                    // secure document software
                    case 'qf0PmsborKWu':
                        sotOnboardingDTO.secureDocumentSoftware = getValueFromAnswer(it)
                        break

                    // secure document url
                    case 'nKRcTLimERbf':
                        sotOnboardingDTO.secureDocumentURL = getValueFromAnswer(it)
                        break

                    // regulatory issues
                    case 'vj1ltyIU9zyE':
                        sotOnboardingDTO.regulatoryIssues = getValueFromAnswer(it)
                        break

                    // bank name
                    case 'Auvxl67FasFA':
                        sotOnboardingDTO.bankName = getValueFromAnswer(it)
                        break

                    // cc company
                    case 'rR6QW5TpKSKk':
                        sotOnboardingDTO.ccCompany = getValueFromAnswer(it)
                        break

                    // financial model development
                    case 'LS3heLhj7j9p':
                        sotOnboardingDTO.financialModelDevelopment = getValueFromAnswer(it)
                        break

                    // number of employees
                    case '4459wAm52sic':
                        sotOnboardingDTO.numberOfEmployees = getValueFromAnswer(it)
                        break

                    // payroll software
                    case 'gctOuiXXCW5B':
                        sotOnboardingDTO.payrollSoftware = getValueFromAnswer(it)
                        break

                    // number of contractors
                    case 'xxoQHKlsBaWz':
                        sotOnboardingDTO.numberOfContractors = getValueFromAnswer(it)
                        break

                    // contractor location
                    case 'q6rKF1ZG3nqN':
                        sotOnboardingDTO.contractorLocation = getValueFromAnswer(it)
                        break

                    // contractor payment process
                    case 'HLhg8xyLb6ir':
                        sotOnboardingDTO.contractorPaymentProcess = getValueFromAnswer(it)
                        break

                    // password manager
                    case '3b5CBBlAsloe':
                        sotOnboardingDTO.passwordManager = getValueFromAnswer(it)
                        break
                }

                // set the company name from the hidden value
                sotOnboardingDTO.alsoKnownAs = typeFormOnboardRequest.form_response.hidden.customer_name
            }
        }
        return sotOnboardingDTO
    }

    private static String getValueFromAnswer(Map answerMap) {
        String answer = ''
        switch( answerMap.type ) {
            case 'text':
                answer = answerMap.text
                break

            case 'choice':
                answer = answerMap.choice.label
                break

            case 'file_url':
                answer = answerMap.file_url
                break
            default:
                answer = answerMap.type + 'not defined'
        }

        return answer
    }
}
