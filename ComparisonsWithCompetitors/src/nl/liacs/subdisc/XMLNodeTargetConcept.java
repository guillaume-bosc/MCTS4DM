/*
 * TODO the values index, name, short and type can be taken from the Table, so
 * setting only the name in the TargetConcept Node is enough to retrieve those
 * values. ---
 * The Nodes that will be created in the TargetConcept Node.
 */
package nl.liacs.subdisc;

// TODO class may be removed, this XML parsing/ creation strategy is not used
public enum XMLNodeTargetConcept
{
		NR_TARGET_ATTRIBUTES
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getNrTargetAttributes());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		TARGET_TYPE_NAME
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getTargetType().toString());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		PRIMARY_TARGET
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return toString();
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		PRIMARY_TARGET_INDEX
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getPrimaryTarget().getIndex());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		PRIMARY_TARGET_NAME
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getPrimaryTarget().getName());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		PRIMARY_TARGET_SHORT
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getPrimaryTarget().getShort());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		PRIMARY_TARGET_TYPE
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getPrimaryTarget().getType().toString());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		TARGET_VALUE
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return String.valueOf(theTargetConcept.getTargetValue());
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		SECONDARY_TARGET
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return toString();
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		SECONDARY_TARGET_INDEX
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return TargetType.hasSecondaryTarget(theTargetConcept.getTargetType()) ?
						String.valueOf(theTargetConcept.getSecondaryTarget().getIndex())
						: "";
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		SECONDARY_TARGET_NAME
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return TargetType.hasSecondaryTarget(theTargetConcept.getTargetType()) ?
						String.valueOf(theTargetConcept.getSecondaryTarget().getName())
						: "";
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		SECONDARY_TARGET_SHORT
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return TargetType.hasSecondaryTarget(theTargetConcept.getTargetType()) ?
						String.valueOf(theTargetConcept.getSecondaryTarget().getShort())
						: "";
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		SECONDARY_TARGET_TYPE
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return TargetType.hasSecondaryTarget(theTargetConcept.getTargetType()) ?
						String.valueOf(theTargetConcept.getSecondaryTarget().getType().toString())
						: "";

			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		},
		MULTI_TARGETS
		{
			@Override
			public String getValueFromData(TargetConcept theTargetConcept)
			{
				return toString();	// TODO will change, adding all secondary targets 
			}

			@Override
			public void setValueFromFile(SearchParameters theSearchParameter, String theValue)
			{
			}
		};

		abstract String getValueFromData(TargetConcept theTargetConcept);
		abstract void setValueFromFile(SearchParameters theSearchParameter, String theValue);
}
