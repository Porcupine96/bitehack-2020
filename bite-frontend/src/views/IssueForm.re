let messages = {
  "issue": {j|New Issue|j},
  "issueTitle": {j|Title|j},
  "issueTime": {j|Issue Occurrence Time|j},
  "reportTime": {j|Issue Report Time|j},
  "precondition": {j|How did you encounter the issue?|j},
  "expected": {j|What is the expected behaviour?|j},
  "actual": {j|What actually happened?|j},
  "additional": {j|Additional information|j},
  "partOfApp": {j|Part of application that was affected|j},
};

module Styles = {
  open Css;

  let mainContainer =
    style([
      height(pct(100.)),
      width(pct(88.)),
      display(`flex),
      alignItems(`center),
      justifyContent(`center),
      marginTop(rem(1.)),
      marginBottom(rem(1.)),
    ]);

  let root =
    style([
      height(pct(100.)),
      width(pct(95.)),
      display(`flex),
      alignItems(`center),
      justifyContent(`center),
    ]);

  let title = style([display(`flex), alignItems(`center)]);

  let bugIcon =
    style([
      fontSize(px(32))->important,
      marginLeft(px(4)),
      padding(px(1)),
    ]);

  let priorityLabel = style([marginLeft(px(16)), marginBottom(px(20))]);

  let priorityContainer = style([marginTop(pt(8)), width(pct(100.))]);

  let priority =
    style([
      width(pct(100.)),
      display(`flex),
      alignItems(`center),
      justifyContent(`flexStart),
      marginLeft(pt(-7)),
      flexDirection(`row)->important,
    ]);

  let image = style([width(px(10)), height(px(10))]);

  let cancelButton = ReactDOMRe.Style.make(~color="white", ~backgroundColor="#E5493A", ());

  let reportButton = ReactDOMRe.Style.make(~color="white", ~backgroundColor="#0747A6", ());

  let lowPriority = ReactDOMRe.Style.make(~color="lightgreen", ());

  let mediumPriority = ReactDOMRe.Style.make(~color="orange", ());

  let highPriority = ReactDOMRe.Style.make(~color="red", ());

  let timeMargin =
    style([marginLeft(px(16)), marginRight(px(16)), marginTop(px(8))]);

  let timeContainer =
    style([display(`flex), justifyContent(`spaceBetween)]);

  let time = style([marginLeft(px(16)), marginBottom(px(20))]);

  let margin =
    style([
      width(pct(100.)),
      display(`flex),
      justifyContent(`center),
      alignItems(`center),
      marginTop(px(32)),
    ]);

  let buttonMargin =
    style([
      width(pct(100.)),
      display(`flex),
      justifyContent(`center),
      alignItems(`center),
    ]);

  let buttonContainer =
    style([
      width(pct(65.)),
      display(`flex),
      justifyContent(`spaceBetween),
    ]);
};

module FormConfig = {
  open Formality;

  type field =
    | Name
    | Priority
    | Precondition
    | Expected
    | IssueTime
    | ReportTime
    | Actual
    | Additional
    | ErrorDate
    | PartOfApp;

  type priority =
    | High
    | Medium
    | Low;

  type state = {
    name: string,
    precondition: string,
    priority,
    expected: string,
    actual: string,
    additional: string,
    partOfApp: string,
    errorDate: string,
  };

  type submissionError = unit;
  type message = string;

  module NameField = {
    let update = (name: string, state) => {...state, name};

    let validator = {
      field: Name,
      strategy: Strategy.OnFirstSuccessOrFirstBlur,
      dependents: None,
      validate: ({name, _}) => Validation.Text.validate(name),
    };
  };

  module PartOfAppField = {
    let update = (partOfApp: string, state) => {...state, partOfApp};

    let validator = {
      field: PartOfApp,
      strategy: Strategy.OnFirstSuccessOrFirstBlur,
      dependents: None,
      validate: ({partOfApp, _}) => Validation.Text.validate(partOfApp),
    };
  };

  module PreconditionField = {
    let update = (precondition: string, state) => {...state, precondition};

    let validator = {
      field: Precondition,
      strategy: Strategy.OnFirstSuccessOrFirstBlur,
      dependents: None,
      validate: ({precondition, _}) =>
        Validation.Text.validate(precondition),
    };
  };

  module ExpectedField = {
    let update = (expected: string, state) => {...state, expected};

    let validator = {
      field: Expected,
      strategy: Strategy.OnFirstSuccessOrFirstBlur,
      dependents: None,
      validate: ({expected, _}) => Validation.Text.validate(expected),
    };
  };

  module PriorityField = {
    let update = (s, state) => {
      let priority =
        switch (s) {
        | "o" => Low
        | "e" => Medium
        | "i" => High
        | _ => Medium
        };
      {...state, priority};
    };
  };

  module ActualField = {
    let update = (actual: string, state) => {...state, actual};

    let validator = {
      field: Actual,
      strategy: Strategy.OnFirstSuccessOrFirstBlur,
      dependents: None,
      validate: ({actual, _}) => Validation.Text.validate(actual),
    };
  };

  module AdditionalField = {
    let update = (additional: string, state) => {...state, additional};

    let validator = {
      field: Additional,
      strategy: Strategy.OnFirstSuccessOrFirstBlur,
      dependents: None,
      validate: ({additional, _}) => Validation.Text.validate(additional),
    };
  };

  let initialState = issue =>
    switch (issue) {
    | Some(issue) => {
        name: issue##name,
        precondition: issue##precondition,
        priority: Medium,
        expected: issue##expected,
        actual: issue##actual,
        additional: issue##additional,
        partOfApp: issue##partOfApp,
        errorDate: issue##errorDate,
      }
    | None => {
        name: "",
        precondition: "",
        priority: Medium,
        expected: "",
        actual: "",
        additional: "",
        partOfApp: "",
        errorDate: "2020-01-12T10:32",
      }
    };

  let validators = [];
};

module Reform = Formality.Make(FormConfig);

[@react.component]
let make = (~state: FormConfig.state) => {
  open FormConfig;

  let rawPriority =
    fun
    | Low => "LOW"
    | Medium => "MEDIUM"
    | High => "HIGH";

  let form =
    Reform.useForm(~initialState=state, ~onSubmit=(state, _) =>
      RestApi.post(
        ~path="/confirm",
        ~bodyJson={
          "name": state.name,
          "precondition": state.precondition,
          "expected": state.expected,
          "actual": state.actual,
          "priority": state.priority->rawPriority,
          "partOfApp": state.partOfApp,
          "description": state.additional,
          "errorDate": state.errorDate,
        },
        (),
      )
      ->Promise.Js.get(result => {
           Js.log(result);

           MessengerExtensions.closeWindow("facebook", error =>
             Js.log2("close failed because of", error)
           );
         })
    );

  let update = (~field, update, value) => {
    form.dismissSubmissionResult();
    form.change(field, update(value, form.state));
  };

  let toKey =
    fun
    | Low => 0
    | Medium => 1
    | High => 2;

  <div className=Styles.mainContainer>
    <div className=Styles.root>
      <MaterialUi.CssBaseline />
      <Form onSubmit={form.submit}>
        <div className=Styles.title>
          <Text message=messages##issue variant=`H5 />
          <MaterialUi.Icon className=Styles.bugIcon>
            {ReasonReact.string("bug_report")}
          </MaterialUi.Icon>
        </div>
        <TextInput
          error={form.result(Name)}
          required=true
          onChange={update(~field=Name, FormConfig.NameField.update)}
          label={Some(ReasonReact.string(messages##issueTitle))}
          value={`String(form.state.name)}
          onBlur={_ => form.blur(Name)}
        />
        <TextInput
          error={form.result(PartOfApp)}
          required=true
          onChange={update(
            ~field=PartOfApp,
            FormConfig.PartOfAppField.update,
          )}
          label={Some(ReasonReact.string(messages##partOfApp))}
          value={`String(form.state.partOfApp)}
          onBlur={_ => form.blur(PartOfApp)}
          multiline=true
          rowsMax={`Int(4)}
        />
        <MaterialUi.FormControl className=Styles.priorityContainer>
          <MaterialUi.FormLabel className=Styles.priorityLabel>
            {ReasonReact.string("Priority")}
          </MaterialUi.FormLabel>
          <MaterialUi.RadioGroup
            className=Styles.priority
            value={`Int(toKey(form.state.priority))}
            onChange={(_, k) =>
              update(~field=Priority, PriorityField.update, k)
            }>
            <MaterialUi.FormControlLabel
              value="low"
              control={
                <MaterialUi.Radio
                  checked={`Bool(form.state.priority == Low)}
                  style=Styles.lowPriority
                />
              }
              label={ReasonReact.string("low")}
              labelPlacement=`Top
            />
            <MaterialUi.FormControlLabel
              value="medium"
              control={
                <MaterialUi.Radio
                  checked={`Bool(form.state.priority == Medium)}
                  style=Styles.mediumPriority
                />
              }
              label={ReasonReact.string("medium")}
              labelPlacement=`Top
            />
            <MaterialUi.FormControlLabel
              value="high"
              control={
                <MaterialUi.Radio
                  checked={`Bool(form.state.priority == High)}
                  style=Styles.highPriority
                />
              }
              label={ReasonReact.string("high")}
              labelPlacement=`Top
            />
          </MaterialUi.RadioGroup>
        </MaterialUi.FormControl>
        <TextInput
          error={form.result(Precondition)}
          required=true
          onChange={update(
            ~field=Precondition,
            FormConfig.PreconditionField.update,
          )}
          label={Some(ReasonReact.string(messages##precondition))}
          value={`String(form.state.precondition)}
          onBlur={_ => form.blur(Precondition)}
          multiline=true
          rowsMax={`Int(4)}
        />
        <TextInput
          error={form.result(Expected)}
          required=true
          onChange={update(~field=Expected, FormConfig.ExpectedField.update)}
          label={Some(ReasonReact.string(messages##expected))}
          value={`String(form.state.expected)}
          onBlur={_ => form.blur(Expected)}
          multiline=true
          rowsMax={`Int(4)}
        />
        <TextInput
          error={form.result(Actual)}
          required=true
          onChange={update(~field=Actual, FormConfig.ActualField.update)}
          label={Some(ReasonReact.string(messages##actual))}
          value={`String(form.state.actual)}
          onBlur={_ => form.blur(Actual)}
          multiline=true
          rowsMax={`Int(4)}
        />
        <div className=Styles.timeMargin>
          <MaterialUi.TextField
            className=Styles.time
            type_="datetime-local"
            defaultValue={`String(form.state.errorDate)}
            label={ReasonReact.string(messages##issueTime)}
          />
        </div>
        /* <div className=Styles.timeMargin> */
        /*   <div className=Styles.timeContainer> */
        /*     <MaterialUi.TextField */
        /*       className=Styles.time */
        /*       type_="datetime-local" */
        /*       defaultValue={`String(form.state.errorDate)} */
        /*       label={ReasonReact.string(messages##issueTime)} */
        /*     /> */
        /*     <MaterialUi.TextField */
        /*       className=Styles.time */
        /*       type_="datetime-local" */
        /*       defaultValue={`String("2020-01-12T10:32")} */
        /*       label={ReasonReact.string(messages##reportTime)} */
        /*       disabled=true */
        /*     /> */
        /*   </div> */
        /* </div> */
        <TextInput
          error={form.result(Additional)}
          required=true
          onChange={update(
            ~field=Additional,
            FormConfig.AdditionalField.update,
          )}
          label={Some(ReasonReact.string(messages##additional))}
          value={`String(form.state.additional)}
          onBlur={_ => form.blur(Additional)}
          multiline=true
          rowsMax={`Int(4)}
        />
        <div className=Styles.margin>
          <div className=Styles.buttonMargin>
            <div className=Styles.buttonContainer>
              <MaterialUi.Button
                /* style=Styles.cancelButton */
                color=`Secondary
                variant=`Contained
                onClick={_ =>
                  MessengerExtensions.closeWindow("facebook", error =>
                    Js.log2("close failed because of", error)
                  )
                  ->ignore
                }>
                {ReasonReact.string("Cancel")}
              </MaterialUi.Button>
              <MaterialUi.Button
                  /* style=Styles.reportButton */
                color=`Primary
                variant=`Contained
                type_="submit"
                onClick={_ => form.submit()}>
                {ReasonReact.string("Report")}
              </MaterialUi.Button>
            </div>
          </div>
        </div>
      </Form>
    </div>
  </div>;
};
