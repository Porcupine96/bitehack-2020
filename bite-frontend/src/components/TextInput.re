[@react.component]
let make =
    (
      ~className=?,
      ~inputProps=?,
      ~disabled=false,
      ~value,
      ~error,
      ~onBlur,
      ~onChange,
      ~type_="text",
      ~required=true,
      ~fullWidth=true,
      ~label,
      ~multiline=false,
      ~rowsMax=`Int(1),
      (),
    ) => {
  <FormControl error disabled required>
      <MaterialUi.TextField
        ?inputProps
        ?className
        disabled
        ?label
        onBlur
        onChange={event => event->ReactEvent.Form.target##value->onChange}
        value
        type_
        fullWidth
        error={error->Form.Helpers.hasError}
        multiline
        rowsMax
      />
    </FormControl>;
};
