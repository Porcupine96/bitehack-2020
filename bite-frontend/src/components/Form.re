open Belt;

module Helpers = {
  let hasError =
    fun
    | Some(Result.Error(_)) => true
    | _ => false;

  let isValid = (fields, result) =>
    fields
    ->List.map(result)
    ->List.every(
        fun
        | Some(Result.Ok(Formality.Valid)) => true
        | _ => false,
      );
};

module S = {
  open Css;

  let formWrapper =
    style([
      width(pct(100.))
    ]);

  let content =
    style([
      padding(18->px),
      width(pct(100.)),
      maxWidth(1000->px),
      margin2(~v=`zero, ~h=`auto),
    ]);
};

[@react.component]
let make = (~onSubmit: unit => unit, ~children, ~className=?, ()) => {
  <form
      className=S.formWrapper
    onSubmit={e => {
      ReactEvent.Form.preventDefault(e);
      onSubmit();
    }}>
    <Paper
      classes=[
        MaterialUi.Paper.Classes.Root(
          Cn.make([S.content, Cn.unpack(className)]),
        ),
      ]>
      children
    </Paper>
  </form>;
};
