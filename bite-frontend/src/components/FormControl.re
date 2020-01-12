open Belt;

module Styles = {
  open Css;

  let container = style([margin(rem(1.0))]);
};

let getError = result =>
  switch (result) {
  | Some(Result.Ok(_)) => None
  | Some(Result.Error(error)) => Some(error)
  | None => None
  };

let getErrorMessage = result =>
  switch (result) {
  /* | Some(error) => <DefinedMessage message=error /> */
  | Some(error) => <div> {ReasonReact.string(error)} </div>
  | None => ReasonReact.null
  };

[@react.component]
let make =
    (~error, ~disabled=false, ~required=false, ~fullWidth=true, ~children, ()) => {
  <div className=Styles.container>
    <MaterialUi.FormControl
      error={error |> getError |> Option.isSome} disabled required fullWidth>
      <div> children </div>
      <MaterialUi.FormHelperText>
        {error |> getError |> getErrorMessage}
      </MaterialUi.FormHelperText>
    </MaterialUi.FormControl>
  </div>;
};
