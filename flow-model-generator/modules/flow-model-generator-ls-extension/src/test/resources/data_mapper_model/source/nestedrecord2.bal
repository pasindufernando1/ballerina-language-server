import ballerina/http;

public type SampleType record {|
    string stfield1;
    int stfield2;
|};

public type SampleRecord record {|
    string srfield1;
    SampleType srfield2;
|};

public type ProgressNote record {|
    string pnfield1;
    SampleType pnfield2;
    SampleRecord[] pnfield3;

|};

public type User record {|
    string name;
|};



service / on new http:Listener(9090) {

    function init() returns error? {
    }

    resource function post getProgressNote(@http:Payload User user) returns ProgressNote|http:InternalServerError {
        do {
            SampleType sampleType = { stfield1: "Sample Type Field 1", stfield2: 123 };
            SampleRecord sampleRecord = { srfield1: "Sample Record Field 1", srfield2: sampleType };
            SampleRecord sampleRecord2 = { srfield1: "Sample Record Field 2", srfield2: sampleType };
            ProgressNote progressNote = {
                pnfield1: "Progress Note Field 1",
                pnfield2: sampleType,
                pnfield3: [sampleRecord, sampleRecord2]
            };
            return progressNote;
        } on fail error e {
            return http:INTERNAL_SERVER_ERROR;
        }
    }
}