open Formality;
open Belt;


module Text = {
  /* let messages = */
  /*   [@intl.messages] */
  /*    { */
  /*     "required": { */
  /*       "id": "field.required", */
  /*       "defaultMessage": {j|Field is required|j}, */
  /*     }, */
  /*   }; */

  let messages = {
      "required": {j|Field is required|j},
    };

  let validate = text =>
    switch (text) {
    | "" => Result.Error(messages##required)
    | _ => Result.Ok(Valid)
    };
};
